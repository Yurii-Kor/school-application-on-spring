#!/usr/bin/env bash

set -euo pipefail

image_name="${1:?Usage: publish_docker_image.sh <image-name> <release-version> <release-sha>}"
release_version="${2:?Usage: publish_docker_image.sh <image-name> <release-version> <release-sha>}"
release_sha="${3:?Usage: publish_docker_image.sh <image-name> <release-version> <release-sha>}"

version_tag="$image_name:$release_version"
latest_tag="$image_name:latest"

fail() {
  echo "::error::$1"
  exit 1
}

inspect_label() {
  local image_tag="$1"
  local label_name="$2"

  docker image inspect "$image_tag" \
    --format "{{ index .Config.Labels \"$label_name\" }}"
}

if [[ ! "$release_version" =~ ^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)$ ]]; then
  fail "Release version must use MAJOR.MINOR.PATCH without a suffix: $release_version"
fi

if [[ -z "$image_name" || "$image_name" == /* || "$image_name" == */ ]]; then
  fail "Docker image name is invalid: $image_name"
fi

for image_tag in "$version_tag" "$latest_tag"; do
  if ! docker image inspect "$image_tag" > /dev/null 2>&1; then
    fail "Docker image was not found locally: $image_tag"
  fi
done

version_image_id="$(
  docker image inspect "$version_tag" \
    --format '{{.Id}}'
)"

latest_image_id="$(
  docker image inspect "$latest_tag" \
    --format '{{.Id}}'
)"

if [[ "$version_image_id" != "$latest_image_id" ]]; then
  fail "Version and latest tags do not reference the same local image."
fi

local_revision="$(
  inspect_label \
    "$version_tag" \
    "org.opencontainers.image.revision"
)"

local_version="$(
  inspect_label \
    "$version_tag" \
    "org.opencontainers.image.version"
)"

if [[ "$local_revision" != "$release_sha" ]]; then
  fail "Local image revision label $local_revision does not match release commit $release_sha."
fi

if [[ "$local_version" != "$release_version" ]]; then
  fail "Local image version label $local_version does not match release version $release_version."
fi

version_action="published"

if docker buildx imagetools inspect "$version_tag" > /dev/null 2>&1; then
  echo "Docker Hub tag already exists and will not be overwritten: $version_tag"

  docker pull "$version_tag"

  remote_revision="$(
    inspect_label \
      "$version_tag" \
      "org.opencontainers.image.revision"
  )"

  remote_version="$(
    inspect_label \
      "$version_tag" \
      "org.opencontainers.image.version"
  )"

  if [[ "$remote_revision" != "$release_sha" ]]; then
    fail "Existing Docker Hub tag $version_tag was built from $remote_revision instead of $release_sha."
  fi

  if [[ "$remote_version" != "$release_version" ]]; then
    fail "Existing Docker Hub tag $version_tag has version label $remote_version instead of $release_version."
  fi

  docker tag "$version_tag" "$latest_tag"

  version_image_id="$(
    docker image inspect "$version_tag" \
      --format '{{.Id}}'
  )"

  version_action="reused"
else
  echo "Publishing immutable release tag: $version_tag"
  docker push "$version_tag"
fi

echo "Publishing moving latest tag: $latest_tag"
docker push "$latest_tag"

echo "Verifying published Docker Hub tags..."

docker buildx imagetools inspect \
  "$version_tag" \
  > /dev/null

docker buildx imagetools inspect \
  "$latest_tag" \
  > /dev/null

image_size="$(
  docker image inspect "$version_tag" \
    --format '{{.Size}}'
)"

image_architecture="$(
  docker image inspect "$version_tag" \
    --format '{{.Architecture}}'
)"

summary=$(cat <<EOF
## Docker Hub publication

| Property | Value |
|---|---|
| Release image | \`$version_tag\` |
| Latest image | \`$latest_tag\` |
| Release tag action | \`$version_action\` |
| Image ID | \`$version_image_id\` |
| Size | $image_size bytes |
| Architecture | \`$image_architecture\` |
| Source commit | \`$release_sha\` |
| Docker Hub publication | Completed |
EOF
)

printf '%s\n' "$summary"

if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
  printf '%s\n' "$summary" >> "$GITHUB_STEP_SUMMARY"
fi
