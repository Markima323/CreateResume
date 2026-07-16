export type Status = 'DRAFT' | 'ANALYZED' | 'PROJECTS_SELECTED' | 'CONTENT_READY' | 'GENERATED'

export interface Application {
  id: string
  jobTitle: string
  companyName: string
  jobDescription: string
  candidateSummary: string
  status: Status
  analysisJson?: string
  analysisEditedJson?: string
  selectedProjectIds: string[]
}

export interface Project {
  id: string
  slug: string
  nameZh: string
  nameDe: string
  projectType: string
  roleText: string
  summary: string
  technologies: string
  responsibilities: string
  outcomes: string
  facts: string
  keywords: string
}

export interface Recommendation {
  project: Project
  score: number
  matchedKeywords: string[]
  reason: string
  gaps: string[]
  source: 'OPENAI' | 'LOCAL_FALLBACK'
}

export interface ParsedProject {
  title: string
  technologies: string
  context: string
  items: string[]
  errors: string[]
  valid: boolean
}

export interface Draft {
  id: string
  position: number
  project: Project
  prompt: string
  latex?: string
  parsed?: ParsedProject
  errors: string[]
  approved: boolean
  updatedAt: string
}

export interface Generation {
  id: string
  status: 'TEX_READY' | 'PDF_READY'
  errorMessage?: string
  createdAt: string
}
