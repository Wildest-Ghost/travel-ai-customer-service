<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { sendMessage, getSessions, getSessionMessages, type SessionItem } from '@/api/chat'
import type { ChatMessage } from '@/types'

const router = useRouter()
const auth = useAuthStore()

const messages = ref<ChatMessage[]>([])
const sessions = ref<SessionItem[]>([])
const input = ref('')
const sending = ref(false)
const msgBox = ref<HTMLElement | null>(null)

// 当前会话 ID（点击侧边栏会切换；新对话会换新的）
const sessionId = ref<string>(localStorage.getItem('sessionId') || crypto.randomUUID())
localStorage.setItem('sessionId', sessionId.value)

function scrollDown() {
  nextTick(() => {
    if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight
  })
}

/** 拉取侧边栏会话列表 */
async function loadSessions() {
  try {
    const { data } = await getSessions()
    if (data.code === 0) sessions.value = data.data
  } catch {
    /* 列表加载失败不影响主流程 */
  }
}

/** 点击侧边栏某个会话 → 切换并加载它的消息 */
async function switchSession(s: SessionItem) {
  sessionId.value = s.sessionId
  localStorage.setItem('sessionId', sessionId.value)
  try {
    const { data } = await getSessionMessages(s.sessionId)
    if (data.code === 0) {
      messages.value = data.data.map((m) => ({
        role: m.role === 'assistant' ? 'bot' : 'user',
        content: m.content
      }))
      scrollDown()
    }
  } catch {
    messages.value = []
  }
}

/** 新对话：换新 sessionId，清空当前界面 */
function newConversation() {
  sessionId.value = crypto.randomUUID()
  localStorage.setItem('sessionId', sessionId.value)
  messages.value = []
  input.value = ''
}

async function send() {
  const text = input.value.trim()
  if (!text || sending.value) return
  const isFirst = messages.value.length === 0
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  sending.value = true
  scrollDown()
  try {
    const { data } = await sendMessage(text, sessionId.value)
    messages.value.push({
      role: 'bot',
      content: data.code === 0 ? data.data : '⚠️ ' + (data.msg || '出错了')
    })
    // 首轮发完，新会话会出现在列表里 → 刷新列表
    if (isFirst) loadSessions()
  } catch (e: unknown) {
    messages.value.push({
      role: 'bot',
      content: '⚠️ 网络错误：' + (e instanceof Error ? e.message : String(e))
    })
  } finally {
    sending.value = false
    scrollDown()
  }
}

function quick(text: string) {
  input.value = text
  send()
}

function logout() {
  auth.logout()
  router.push('/login')
}

onMounted(loadSessions)
</script>

<template>
  <div class="layout">
    <!-- ===== 侧边栏 ===== -->
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-icon">H</div>
        <div>
          <div class="brand-name">旅行智能客服</div>
          <div class="brand-sub">Travel Concierge</div>
        </div>
      </div>

      <button class="btn-new" @click="newConversation">＋ 新对话</button>

      <div class="side-section">
        <div class="side-label">历史会话</div>
        <div class="session-list">
          <div
            v-for="s in sessions"
            :key="s.sessionId"
            class="session-item"
            :class="{ active: s.sessionId === sessionId }"
            @click="switchSession(s)"
          >
            <span class="dot">💬</span>
            <span class="session-title" :title="s.title">{{ s.title }}</span>
          </div>
          <div class="empty-list" v-if="sessions.length === 0">暂无历史会话</div>
        </div>
      </div>

      <div class="side-note">多智能体编排 · RAG 知识库<br />多轮记忆 · 工具调用</div>

      <div class="side-user">
        <div class="user-avatar">{{ (auth.username || '?').charAt(0).toUpperCase() }}</div>
        <div class="user-name" :title="auth.username">{{ auth.username }}</div>
        <button class="btn-logout" @click="logout">退出</button>
      </div>
    </aside>

    <!-- ===== 主区 ===== -->
    <main class="main">
      <header class="topbar">
        <div class="topbar-title">在线客服</div>
        <div class="topbar-status"><i></i> 在线</div>
      </header>

      <div class="messages" ref="msgBox">
        <div class="welcome" v-if="messages.length === 0">
          <div class="welcome-title">您好，{{ auth.username }} 👋</div>
          <div class="welcome-text">很高兴为您服务，请问需要什么帮助？</div>
          <div class="chips">
            <span class="chip" @click="quick('查我的订单')">查我的订单</span>
            <span class="chip" @click="quick('把订单 1 改到产品 3')">改签订单</span>
            <span class="chip" @click="quick('改签收手续费吗')">改签政策</span>
            <span class="chip" @click="quick('退订规则')">退订规则</span>
          </div>
        </div>

        <div class="row" :class="m.role" v-for="(m, i) in messages" :key="i">
          <div class="avatar" :class="m.role">{{ m.role === 'user' ? '我' : '🛎️' }}</div>
          <div class="bubble">{{ m.content }}</div>
        </div>

        <div class="row bot" v-if="sending">
          <div class="avatar bot">🛎️</div>
          <div class="bubble"><span class="typing"><i></i><i></i><i></i></span></div>
        </div>
      </div>

      <div class="input-bar">
        <textarea
          v-model="input"
          rows="1"
          placeholder="请输入您的问题（回车发送，Shift+回车换行）"
          @keydown.enter.exact.prevent="send"
        ></textarea>
        <button :disabled="sending || !input.trim()" @click="send">发送</button>
      </div>
    </main>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  width: min(1060px, 96vw);
  height: min(840px, 94vh);
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.35);
}

/* ===== 侧边栏：藏蓝 ===== */
.sidebar {
  width: 256px;
  flex-shrink: 0;
  background: linear-gradient(180deg, #14213d 0%, #1b2b4b 100%);
  color: #d9dde6;
  display: flex;
  flex-direction: column;
  padding: 22px 18px;
}
.brand { display: flex; align-items: center; gap: 12px; margin-bottom: 22px; }
.brand-icon {
  width: 40px;
  height: 40px;
  border-radius: 9px;
  background: linear-gradient(135deg, #c8a25b, #b08d44);
  color: #14213d;
  font-weight: 800;
  font-size: 22px;
  font-family: Georgia, serif;
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-name { font-size: 16px; font-weight: 600; color: #fff; letter-spacing: 0.5px; }
.brand-sub { font-size: 11px; color: #8a93a6; letter-spacing: 1px; margin-top: 2px; }

.btn-new {
  width: 100%;
  padding: 11px;
  border: 1px solid #c8a25b;
  background: transparent;
  color: #d8b878;
  border-radius: 9px;
  font-size: 14px;
  cursor: pointer;
  transition: 0.15s;
}
.btn-new:hover { background: rgba(200, 162, 91, 0.12); }

.side-section { margin-top: 22px; flex: 1; min-height: 0; display: flex; flex-direction: column; }
.side-label { font-size: 11px; color: #6f7a90; letter-spacing: 1px; margin-bottom: 10px; }
.session-list { overflow-y: auto; flex: 1; display: flex; flex-direction: column; gap: 4px; }
.session-item {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 10px 11px;
  border-radius: 8px;
  cursor: pointer;
  transition: 0.12s;
  color: #c2c9d6;
}
.session-item:hover { background: rgba(255, 255, 255, 0.07); }
.session-item.active { background: rgba(200, 162, 91, 0.16); color: #f0e6d2; }
.dot { font-size: 13px; flex-shrink: 0; }
.session-title { font-size: 13px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.empty-list { font-size: 12px; color: #6f7a90; padding: 8px 4px; }

.side-note {
  font-size: 11.5px;
  color: #6f7a90;
  line-height: 1.7;
  padding: 14px 0;
  border-top: 1px solid rgba(255, 255, 255, 0.07);
}
.side-user { display: flex; align-items: center; gap: 10px; }
.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #c8a25b;
  color: #14213d;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.user-name { flex: 1; font-size: 13px; color: #eef0f4; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.btn-logout {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #c0c7d4;
  padding: 5px 10px;
  border-radius: 7px;
  font-size: 12px;
  cursor: pointer;
}
.btn-logout:hover { border-color: #c8a25b; color: #d8b878; }

/* ===== 主区：米白 ===== */
.main { flex: 1; display: flex; flex-direction: column; background: #f7f4ee; min-width: 0; }
.topbar {
  padding: 16px 24px;
  background: #fff;
  border-bottom: 1px solid #ece7dd;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.topbar-title { font-size: 16px; font-weight: 600; color: #1b2b4b; font-family: Georgia, 'Microsoft YaHei', serif; }
.topbar-status { font-size: 12.5px; color: #5b9e6e; display: flex; align-items: center; gap: 6px; }
.topbar-status i { width: 8px; height: 8px; border-radius: 50%; background: #5b9e6e; box-shadow: 0 0 0 3px rgba(91, 158, 110, 0.18); }

.messages { flex: 1; overflow-y: auto; padding: 26px; display: flex; flex-direction: column; gap: 16px; }
.row { display: flex; gap: 11px; align-items: flex-end; }
.row.user { flex-direction: row-reverse; }
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
}
.avatar.bot { background: #14213d; }
.avatar.user { background: #c8a25b; color: #14213d; font-size: 14px; font-weight: 600; }
.bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 14px;
  line-height: 1.65;
  font-size: 14.5px;
  white-space: pre-wrap;
  word-break: break-word;
}
.row.bot .bubble { background: #fff; border: 1px solid #ece7dd; color: #2d2d2d; border-bottom-left-radius: 4px; }
.row.user .bubble { background: #1b2b4b; color: #f3f1ea; border-bottom-right-radius: 4px; }

.typing { display: inline-flex; gap: 5px; padding: 4px 2px; }
.typing i { width: 7px; height: 7px; border-radius: 50%; background: #c8a25b; animation: blink 1.2s infinite; }
.typing i:nth-child(2) { animation-delay: 0.2s; }
.typing i:nth-child(3) { animation-delay: 0.4s; }
@keyframes blink {
  0%, 80%, 100% { opacity: 0.25; transform: scale(0.8); }
  40% { opacity: 1; transform: scale(1); }
}

.welcome { margin: auto; text-align: center; max-width: 90%; }
.welcome-title { font-size: 22px; color: #1b2b4b; font-family: Georgia, 'Microsoft YaHei', serif; }
.welcome-text { color: #8a8270; font-size: 14px; margin-top: 8px; }
.chips { margin-top: 20px; display: flex; flex-wrap: wrap; gap: 10px; justify-content: center; }
.chip {
  background: #fff;
  border: 1px solid #e0d9cc;
  border-radius: 18px;
  padding: 8px 15px;
  font-size: 13px;
  color: #5a5346;
  cursor: pointer;
  transition: 0.15s;
}
.chip:hover { background: #14213d; color: #d8b878; border-color: #14213d; }

.input-bar { padding: 16px 20px; border-top: 1px solid #ece7dd; display: flex; gap: 12px; background: #fff; }
.input-bar textarea {
  flex: 1;
  resize: none;
  padding: 12px 15px;
  border: 1px solid #ded6c8;
  border-radius: 11px;
  font-size: 14.5px;
  font-family: inherit;
  outline: none;
  max-height: 120px;
  line-height: 1.4;
  background: #fcfbf8;
}
.input-bar textarea:focus { border-color: #c8a25b; }
.input-bar button {
  padding: 0 26px;
  border: none;
  border-radius: 11px;
  background: #14213d;
  color: #fff;
  font-weight: 600;
  cursor: pointer;
  transition: 0.15s;
}
.input-bar button:hover:not(:disabled) { background: #1b2b4b; }
.input-bar button:disabled { background: #c9c3b6; cursor: not-allowed; }
</style>
