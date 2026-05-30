import request from './request'
import type { Result } from '@/types'

/** 一个会话（侧边栏列表项） */
export interface SessionItem {
  id: number
  sessionId: string
  userId: string
  title: string
  createdAt: string
  updatedAt: string
}

/** 一条历史消息 */
export interface HistoryMessage {
  id: number
  sessionId: string
  role: string
  content: string
  createdAt: string
}

/** 发送一条消息给 AI 客服。sessionId 用于多轮记忆 */
export function sendMessage(message: string, sessionId: string) {
  return request.post<Result<string>>('/chat', { message, sessionId })
}

/** 当前用户的所有会话（ChatGPT 侧边栏列表） */
export function getSessions() {
  return request.get<Result<SessionItem[]>>('/chat/sessions')
}

/** 某个会话的全部消息 */
export function getSessionMessages(sessionId: string) {
  return request.get<Result<HistoryMessage[]>>(`/chat/sessions/${sessionId}/messages`)
}
