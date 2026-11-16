#!/usr/bin/env bash
# GitHub Actions gọi script này qua SSH sau khi đẩy image mới lên ghcr.io.
# Chạy tay cũng được: /opt/toeic/eo-be/deploy/deploy.sh
set -euo pipefail

cd "$(dirname "$0")"

# Lấy bản mới của compose/Caddyfile/script trong repo.
git -C .. pull --ff-only --quiet

compose() {
  docker compose -f docker-compose.prod.yml "$@"
}

compose pull --quiet
compose up -d --remove-orphans
docker image prune -f >/dev/null

# Đợi FE trả lời qua Caddy rồi mới báo thành công.
for _ in $(seq 1 60); do
  if curl -fsS -o /dev/null http://localhost/; then
    echo "Deploy xong."
    compose ps
    exit 0
  fi
  sleep 3
done

echo "Trang web chưa trả lời sau 3 phút." >&2
compose ps >&2
compose logs --tail 50 be fe >&2
exit 1
