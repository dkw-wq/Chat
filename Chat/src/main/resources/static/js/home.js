const nickname = document.querySelector("#nickname");
const username = document.querySelector("#username");
const logoutButton = document.querySelector("#logoutButton");

async function loadCurrentUser() {
  const response = await fetch("/api/auth/me");
  if (!response.ok) {
    window.location.href = "/login.html";
    return;
  }

  const user = await response.json();
  nickname.textContent = `${user.nickname}，你好`;
  username.textContent = `账号：${user.username}`;
}

logoutButton.addEventListener("click", async () => {
  await fetch("/api/auth/logout", {
    method: "POST"
  });
  window.location.href = "/login.html";
});

loadCurrentUser();
