#!/usr/bin/env bash
# Chạy MỘT LẦN trên VPS mới (Ubuntu/Debian), đăng nhập bằng root:
#   curl -fsSL https://raw.githubusercontent.com/NTrungHieu05/eo-be/main/deploy/bootstrap.sh | bash -s -- "<public key deploy>"
#
# Việc làm: cài Docker, thêm swap, tạo user "deploy" cho GitHub Actions SSH vào,
# clone eo-be vào /opt/toeic, tạo deploy/.env với JWT_SECRET ngẫu nhiên, mở tường lửa 22/80/443.
# Chạy lại nhiều lần không sao: bước nào đã làm thì bỏ qua.
set -euo pipefail

PUBKEY="${1:?Thiếu public key. Dùng: bash -s -- \"ssh-ed25519 AAAA... toeic-deploy\"}"
APP_DIR=/opt/toeic
REPO=https://github.com/NTrungHieu05/eo-be.git

if [ "$(id -u)" -ne 0 ]; then
  echo "Hãy chạy bằng root." >&2
  exit 1
fi

echo "== Gói hệ thống"
export DEBIAN_FRONTEND=noninteractive
apt-get update -qq
apt-get install -y -qq git curl ufw openssl ca-certificates >/dev/null

echo "== Docker"
if ! command -v docker >/dev/null 2>&1; then
  curl -fsSL https://get.docker.com | sh
fi
systemctl enable --now docker >/dev/null

echo "== Swap 2 GB"
if ! swapon --show | grep -q .; then
  fallocate -l 2G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile >/dev/null
  swapon /swapfile
  grep -q '^/swapfile ' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

echo "== User deploy"
id deploy >/dev/null 2>&1 || useradd -m -s /bin/bash deploy
usermod -aG docker deploy
install -d -m 700 -o deploy -g deploy /home/deploy/.ssh
touch /home/deploy/.ssh/authorized_keys
grep -qxF "$PUBKEY" /home/deploy/.ssh/authorized_keys || echo "$PUBKEY" >> /home/deploy/.ssh/authorized_keys
chown deploy:deploy /home/deploy/.ssh/authorized_keys
chmod 600 /home/deploy/.ssh/authorized_keys

echo "== Mã nguồn và dữ liệu ở $APP_DIR"
install -d -o deploy -g deploy "$APP_DIR" "$APP_DIR/data" "$APP_DIR/data/images" "$APP_DIR/data/sounds" "$APP_DIR/data/files"
if [ ! -d "$APP_DIR/eo-be/.git" ]; then
  sudo -u deploy git clone --quiet "$REPO" "$APP_DIR/eo-be"
fi

ENV_FILE="$APP_DIR/eo-be/deploy/.env"
if [ ! -f "$ENV_FILE" ]; then
  # MongoDB 5 trở lên cần CPU có AVX; VPS rẻ đôi khi không có.
  if grep -qw avx /proc/cpuinfo; then MONGO_IMAGE=mongo:7; else MONGO_IMAGE=mongo:4.4; fi
  cat > "$ENV_FILE" <<EOF
# Sinh bởi bootstrap.sh. Không commit file này.
JWT_SECRET=$(openssl rand -hex 32)
# ":80" = chạy bằng IP. Có tên miền thì đổi thành tên miền để có HTTPS.
SITE_ADDRESS=:80
MONGO_IMAGE=$MONGO_IMAGE
DATA_DIR=$APP_DIR/data
# Gửi mail quên mật khẩu (Gmail App Password). Để trống thì mã reset chỉ ghi vào log.
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_FROM=
EOF
  chown deploy:deploy "$ENV_FILE"
  chmod 600 "$ENV_FILE"
fi
chmod +x "$APP_DIR/eo-be/deploy/deploy.sh"

echo "== Tường lửa"
ufw allow OpenSSH >/dev/null
ufw allow 80/tcp >/dev/null
ufw allow 443/tcp >/dev/null
ufw --force enable >/dev/null

echo
echo "Xong. Lần deploy đầu sẽ chạy khi GitHub Actions push image, hoặc chạy tay:"
echo "  sudo -u deploy $APP_DIR/eo-be/deploy/deploy.sh"
