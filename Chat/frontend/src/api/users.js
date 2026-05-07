async function request(path, options = {}) {
  const response = await fetch(path, {
    credentials: "include",
    ...options,
    headers: {
      ...(options.headers || {})
    }
  });

  const contentType = response.headers.get("content-type") || "";
  const body = contentType.includes("application/json")
    ? await response.json()
    : {};

  if (!response.ok) {
    throw new Error(body.message || "请求失败");
  }

  return body;
}

export function getMiniProfile(userId) {
  return request(`/api/users/${userId}/mini-profile`);
}

export function uploadAvatar(file) {
  const formData = new FormData();
  formData.append("avatar", file);

  return request("/api/users/me/avatar", {
    method: "POST",
    body: formData
  });
}
