#!/usr/bin/env bash
# Sao lưu MongoDB và thư mục ảnh/âm thanh trên VPS.
# deploy.sh cài lịch chạy 02:30 mỗi ngày (crontab của user deploy). Chạy tay cũng được:
#   /opt/toeic/eo-be/deploy/backup.sh
#
# Khôi phục:
#   cd /opt/toeic/eo-be/deploy
#   docker compose -f docker-compose.prod.yml exec -T mongo \
#     mongorestore --archive --gzip --drop < /opt/toeic/backups/mongo-<thời điểm>.archive.gz
#   tar -xzf /opt/toeic/backups/data-<thời điểm>.tar.gz -C /opt/toeic
set -euo pipefail

cd "$(dirname "$0")"

DEST="${BACKUP_DIR:-/opt/toeic/backups}"
KEEP="${BACKUP_KEEP:-7}"
STAMP="$(date +%Y%m%d-%H%M%S)"
DATA_DIR="$(grep -E '^DATA_DIR=' .env 2>/dev/null | cut -d= -f2- || true)"
DATA_DIR="${DATA_DIR:-/opt/toeic/data}"

mkdir -p "$DEST"

# Ghi ra tệp tạm rồi mới đổi tên, để một lần sao lưu lỗi không để lại tệp dở dang.
docker compose -f docker-compose.prod.yml exec -T mongo \
  mongodump --archive --gzip --db exam_online > "$DEST/.mongo-$STAMP.tmp"
mv "$DEST/.mongo-$STAMP.tmp" "$DEST/mongo-$STAMP.archive.gz"

tar -czf "$DEST/.data-$STAMP.tmp" -C "$(dirname "$DATA_DIR")" "$(basename "$DATA_DIR")"
mv "$DEST/.data-$STAMP.tmp" "$DEST/data-$STAMP.tar.gz"

# Giữ KEEP bản mới nhất của mỗi loại.
ls -1t "$DEST"/mongo-*.archive.gz 2>/dev/null | tail -n +"$((KEEP + 1))" | xargs -r rm -f
ls -1t "$DEST"/data-*.tar.gz 2>/dev/null | tail -n +"$((KEEP + 1))" | xargs -r rm -f

echo "$(date '+%F %T') Sao lưu xong: mongo-$STAMP.archive.gz, data-$STAMP.tar.gz"
