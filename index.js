const express = require('express');
const cors = require('cors');
const { Pool } = require('pg');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  ssl: { rejectUnauthorized: false }
});

// Health check endpoint
app.get('/', (req, res) => res.send('RewardRush API Running'));

// 1. User Profile / Points Check
app.get('/api/user/:phone', async (req, res) => {
  try {
    const { phone } = req.params;
    let result = await pool.query('SELECT * FROM users WHERE phone = $1', [phone]);
    
    // User లేకపోతే ఆటోమేటిక్‌గా క్రియేట్ చేయడం
    if (result.rows.length === 0) {
      const refCode = 'RR' + Math.random().toString(36).substring(2, 6).toUpperCase();
      result = await pool.query(
        'INSERT INTO users (phone, referral_code) VALUES ($1, $2) RETURNING *',
        [phone, refCode]
      );
    }
    res.json(result.rows[0]);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 2. Atomic Voucher Redemption
app.post('/api/redeem', async (req, res) => {
  const { userId, brand, costPoints } = req.body;
  const client = await pool.connect();

  try {
    await client.query('BEGIN');

    // User balance lock & check
    const userRes = await client.query('SELECT points FROM users WHERE id = $1 FOR UPDATE', [userId]);
    if (!userRes.rows[0] || userRes.rows[0].points < costPoints) {
      throw new Error('INSUFFICIENT_POINTS');
    }

    // Single voucher pick & lock
    const voucherRes = await client.query(
      'SELECT id, card_number, pin FROM vouchers_pool WHERE brand = $1 AND is_used = FALSE LIMIT 1 FOR UPDATE SKIP LOCKED',
      [brand]
    );

    if (voucherRes.rows.length === 0) {
      throw new Error('OUT_OF_STOCK');
    }

    const voucher = voucherRes.rows[0];

    // Deduct points
    await client.query('UPDATE users SET points = points - $1 WHERE id = $2', [costPoints, userId]);

    // Mark voucher used
    await client.query(
      'UPDATE vouchers_pool SET is_used = TRUE, assigned_to = $1, redeemed_at = NOW() WHERE id = $2',
      [userId, voucher.id]
    );

    // Ledger entry
    await client.query(
      'INSERT INTO points_ledger (user_id, points, type, description) VALUES ($1, $2, $3, $4)',
      [userId, -costPoints, 'REDEMPTION', `Claimed ${brand}`]
    );

    await client.query('COMMIT');
    res.json({ success: true, voucher: { cardNumber: voucher.card_number, pin: voucher.pin } });
  } catch (err) {
    await client.query('ROLLBACK');
    res.status(400).json({ error: err.message });
  } finally {
    client.release();
  }
});

// 3. User Vault Items
app.get('/api/vault/:userId', async (req, res) => {
  try {
    const { userId } = req.params;
    const result = await pool.query(
      'SELECT brand, card_number, pin, amount, redeemed_at FROM vouchers_pool WHERE assigned_to = $1 ORDER BY redeemed_at DESC',
      [userId]
    );
    res.json(result.rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Server on port ${PORT}`));
