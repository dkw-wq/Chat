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

export function listChatMessages(friendId, limit = 50) {
  return request(`/api/chats/${friendId}/messages?limit=${limit}`);
}

export function sendChatMessage(friendId, content) {
  return request(`/api/chats/${friendId}/messages`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({ content })
  });
}
