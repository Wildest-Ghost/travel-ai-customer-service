// 后端统一返回结构（对应 common 模块的 Result<T>）
export interface Result<T = unknown> {
  code: number
  msg: string
  data: T
}

// 一条聊天消息
export interface ChatMessage {
  role: 'user' | 'bot'
  content: string
}
