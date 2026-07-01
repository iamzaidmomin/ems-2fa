#!/bin/sh
set -eu

export BAO_ADDR="${BAO_ADDR:-http://127.0.0.1:8200}"
export BAO_TOKEN="${BAO_TOKEN:-root}"

until wget -qO- "$BAO_ADDR/v1/sys/health" >/dev/null 2>&1; do
  sleep 1
done

bao kv put secret/smtp \
  SMTP_HOST="${SMTP_HOST:-smtp.gmail.com}" \
  SMTP_PORT="${SMTP_PORT:-587}" \
  SMTP_USER="${SMTP_USER:-demo@example.com}" \
  SMTP_PASSWORD="${SMTP_PASSWORD:-demo-password}" \
  SMTP_FROM="${SMTP_FROM:-demo@example.com}" >/dev/null 2>&1 || true

echo "Seeded OpenBao secret at secret/smtp"
