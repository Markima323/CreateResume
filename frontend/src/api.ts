import type { Application, Draft, Generation, HistoryEntry, MotivationLetter, Project, Recommendation } from './types'

const BASE = import.meta.env.VITE_API_BASE ?? '/api/v1'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${BASE}${path}`, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...init?.headers },
  })
  if (!response.ok) {
    const problem = await response.json().catch(() => ({}))
    throw new Error(problem.detail ?? problem.title ?? `请求失败 (${response.status})`)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export const api = {
  analyzeRaw: (jobText: string) => request<Application>('/applications/analyze-raw', {
    method: 'POST', body: JSON.stringify({ jobText }),
  }),
  updateAndAnalyzeRaw: (id: string, jobText: string) => request<Application>(`/applications/${id}/analyze-raw`, {
    method: 'PUT', body: JSON.stringify({ jobText }),
  }),
  application: (id: string) => request<Application>(`/applications/${id}`),
  createApplication: (data: object) => request<Application>('/applications', { method: 'POST', body: JSON.stringify(data) }),
  updateApplication: (id: string, data: object) => request<Application>(`/applications/${id}`, { method: 'PATCH', body: JSON.stringify(data) }),
  analyze: (id: string) => request<Application>(`/applications/${id}/analyze`, { method: 'POST' }),
  editAnalysis: (id: string, analysisJson: string) => request<Application>(`/applications/${id}/analysis`, { method: 'PUT', body: JSON.stringify({ analysisJson }) }),
  projects: () => request<Project[]>('/projects'),
  recommendations: (id: string) => request<Recommendation[]>(`/applications/${id}/recommendations`, { method: 'POST' }),
  select: (id: string, projectIds: string[]) => request<Application>(`/applications/${id}/selections`, { method: 'PUT', body: JSON.stringify({ projectIds }) }),
  initDrafts: (id: string) => request<Draft[]>(`/applications/${id}/drafts/prompts`, { method: 'POST' }),
  saveDraft: (id: string, position: number, latex: string, approve: boolean) => request<Draft>(`/applications/${id}/drafts/${position}`, { method: 'PUT', body: JSON.stringify({ latex, approve }) }),
  drafts: (id: string) => request<Draft[]>(`/applications/${id}/drafts`),
  generate: (id: string, version: 'WORK' | 'UPWORK') => request<Generation>(`/applications/${id}/generations`, { method: 'POST', body: JSON.stringify({ version }) }),
  generateManual: (id: string, projects: string[], version: 'WORK' | 'UPWORK') => request<Generation>(`/applications/${id}/generations/manual`, {
    method: 'POST', body: JSON.stringify({ projects, version }),
  }),
  downloadUrl: (applicationId: string, generationId: string, type: 'tex' | 'pdf') => `${BASE}/applications/${applicationId}/generations/${generationId}/resume.${type}`,
  history: () => request<HistoryEntry[]>('/history'),
  deleteHistory: (applicationId: string) => request<void>(`/history/${applicationId}`, { method: 'DELETE' }),
  generation: (applicationId: string, generationId: string) => request<Generation>(`/applications/${applicationId}/generations/${generationId}`),
  generateMotivationLetter: (applicationId: string, generationId: string, personalInfo: string, language: 'DE' | 'EN') =>
    request<MotivationLetter>(`/applications/${applicationId}/generations/${generationId}/motivation-letters`, {
      method: 'POST', body: JSON.stringify({ personalInfo, language }),
    }),
  latestMotivationLetter: (applicationId: string, generationId: string) =>
    request<MotivationLetter | undefined>(`/applications/${applicationId}/generations/${generationId}/motivation-letters/latest`),
  motivationLetterUrl: (applicationId: string, generationId: string, letterId: string) =>
    `${BASE}/applications/${applicationId}/generations/${generationId}/motivation-letters/${letterId}/letter.txt`,
}
