import { useEffect, useMemo, useState } from 'react'
import { AlertCircle, Check, Clipboard, Download, FileText, LoaderCircle, RotateCcw, Sparkles } from 'lucide-react'
import { api } from './api'
import type { Application, Draft, Generation, Project, Recommendation } from './types'

const candidateDefault = `Informatikerin mit Schwerpunkt KI-, Backend- und Systementwicklung. Master Informatik an der Hochschule Darmstadt. Erfahrung mit Java, Spring Boot, React, PostgreSQL, Python, KI-Pipelines, Docker und Systemintegration.`

function App() {
  const [application, setApplication] = useState<Application | null>(null)
  const [projects, setProjects] = useState<Project[]>([])
  const [recommendations, setRecommendations] = useState<Recommendation[]>([])
  const [selected, setSelected] = useState<string[]>([])
  const [drafts, setDrafts] = useState<Draft[]>([])
  const [generation, setGeneration] = useState<Generation | null>(null)
  const [step, setStep] = useState(1)
  const [busy, setBusy] = useState('')
  const [error, setError] = useState('')

  useEffect(() => { api.projects().then(setProjects).catch(e => setError(e.message)) }, [])

  const run = async <T,>(label: string, action: () => Promise<T>, done: (value: T) => void) => {
    setBusy(label); setError('')
    try { done(await action()) } catch (e) { setError(e instanceof Error ? e.message : '操作失败') }
    finally { setBusy('') }
  }

  const createAndAnalyze = (form: JobFormValue) => run(
    'analyze',
    () => api.analyzeRaw(form.jobText, form.candidateSummary),
    app => { setApplication(app); setStep(2) },
  )

  const saveAnalysisAndRecommend = (json: string) => {
    if (!application) return
    run('recommend', async () => {
      const app = await api.editAnalysis(application.id, json)
      const recs = await api.recommendations(app.id)
      return { app, recs }
    }, ({ app, recs }) => {
      setApplication(app); setRecommendations(recs); setSelected(recs.map(r => r.project.id)); setStep(3)
    })
  }

  const confirmSelection = () => {
    if (!application) return
    run('prompts', async () => {
      const app = await api.select(application.id, selected)
      const nextDrafts = await api.initDrafts(application.id)
      return { app, nextDrafts }
    }, ({ app, nextDrafts }) => { setApplication(app); setDrafts(nextDrafts); setStep(4) })
  }

  const saveDraft = (position: number, latex: string, approve: boolean) => {
    if (!application) return
    run(`draft-${position}`, () => api.saveDraft(application.id, position, latex, approve), draft => {
      setDrafts(current => current.map(d => d.position === draft.position ? draft : d))
    })
  }

  const generate = () => {
    if (!application) return
    run('generate', () => api.generate(application.id), value => { setGeneration(value); setStep(5) })
  }

  const reset = () => {
    setApplication(null); setRecommendations([]); setSelected([]); setDrafts([]); setGeneration(null); setError(''); setStep(1)
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand-mark">RW</div>
        <div>
          <p className="eyebrow">RESUME WERKSTATT</p>
          <h1>让每个项目都对准目标岗位</h1>
        </div>
        {application && <button className="ghost-button top-reset" onClick={reset}><RotateCcw size={16}/> 新建</button>}
      </header>

      <main>
        <StepRail current={step} onStep={s => s <= step && setStep(s)} />
        {error && <div className="error-banner"><AlertCircle size={19}/><span>{error}</span></div>}

        <section className="workspace">
          {step === 1 && <JobForm initial={application} busy={busy === 'analyze'} onSubmit={createAndAnalyze} />}
          {step === 2 && application && <AnalysisStep application={application} busy={busy === 'recommend'} onContinue={saveAnalysisAndRecommend} />}
          {step === 3 && <RecommendationStep recommendations={recommendations} projects={projects} selected={selected} setSelected={setSelected} busy={busy === 'prompts'} onContinue={confirmSelection} />}
          {step === 4 && <DraftStep drafts={drafts} busy={busy} onSave={saveDraft} onGenerate={generate} />}
          {step === 5 && application && generation && <ExportStep application={application} generation={generation} />}
        </section>
      </main>
      <footer>AI 负责整理与匹配，事实与最终表述由你确认。</footer>
    </div>
  )
}

const steps = ['岗位输入', '岗位分析', '项目匹配', '项目编辑', '导出简历']
function StepRail({ current, onStep }: { current: number; onStep: (step: number) => void }) {
  return <nav className="step-rail" aria-label="生成步骤">
    {steps.map((label, index) => {
      const number = index + 1; const state = number < current ? 'done' : number === current ? 'active' : ''
      return <button key={label} className={state} disabled={number > current} onClick={() => onStep(number)}>
        <span>{number < current ? <Check size={15}/> : number}</span><b>{label}</b>
      </button>
    })}
  </nav>
}

interface JobFormValue { jobText: string; candidateSummary: string }
function JobForm({ initial, busy, onSubmit }: { initial: Application | null; busy: boolean; onSubmit: (value: JobFormValue) => void }) {
  const [value, setValue] = useState<JobFormValue>({
    jobText: initial?.jobDescription ?? '', candidateSummary: initial?.candidateSummary ?? candidateDefault,
  })
  const update = (key: keyof JobFormValue) => (e: React.ChangeEvent<HTMLTextAreaElement>) => setValue(v => ({ ...v, [key]: e.target.value }))
  return <div className="panel intro-panel">
    <div className="panel-heading">
      <div><p className="section-number">01 / JOB BRIEF</p><h2>粘贴目标岗位</h2><p>系统会先把招聘语言翻译成实际工作，再从项目库寻找证据。</p></div>
      <Sparkles className="heading-icon" size={34}/>
    </div>
    <label>完整招聘信息 <small>可包含岗位名称、公司名称、职责和要求</small><textarea className="job-description" value={value.jobText} onChange={update('jobText')} placeholder="直接粘贴整段 Stellenanzeige，Gemini 会自动提取岗位名称、公司名称和岗位介绍…" /></label>
    <label>个人简述 <small>会用于判断哪些要求已有证据</small><textarea value={value.candidateSummary} onChange={update('candidateSummary')} /></label>
    <div className="action-row"><p>Gemini 会先提取字段，再用中文解释岗位；简历项目仍输出德文。</p><button className="primary-button" disabled={busy || !value.jobText.trim()} onClick={() => onSubmit(value)}>{busy && <LoaderCircle className="spin" size={17}/>}提取并分析</button></div>
  </div>
}

function AnalysisStep({ application, busy, onContinue }: { application: Application; busy: boolean; onContinue: (json: string) => void }) {
  const initial = application.analysisEditedJson ?? application.analysisJson ?? '{}'
  const [json, setJson] = useState(initial)
  const parsed = useMemo(() => { try { return JSON.parse(json) } catch { return null } }, [json])
  return <div className="panel">
    <div className="panel-heading"><div><p className="section-number">02 / ROLE DECODED</p><h2>这份工作实际上要做什么</h2><p>先核对提取结果和分析；你编辑后的版本会成为项目匹配依据。</p></div><span className="status-chip">Gemini 分析</span></div>
    <div className="extracted-job-summary">
      <div><span>岗位名称</span><b>{application.jobTitle}</b></div>
      <div><span>公司名称</span><b>{application.companyName || '未识别'}</b></div>
    </div>
    {parsed ? <AnalysisCards analysis={parsed} /> : <div className="inline-warning">JSON 格式暂时无效，请修正后继续。</div>}
    <details className="json-editor"><summary>编辑结构化分析 JSON</summary><textarea value={json} onChange={e => setJson(e.target.value)} spellCheck={false}/></details>
    <div className="action-row"><p>不确定的要求应保留在 uncertainties，而不是自行补全。</p><button className="primary-button" disabled={busy || !parsed} onClick={() => onContinue(json)}>{busy && <LoaderCircle className="spin" size={17}/>}匹配项目</button></div>
  </div>
}

function AnalysisCards({ analysis }: { analysis: Record<string, unknown> }) {
  const list = (key: string) => Array.isArray(analysis[key]) ? analysis[key] as string[] : []
  return <div className="analysis-grid">
    <article className="analysis-card wide"><h3>日常工作</h3><ol>{list('plainLanguageDuties').map(x => <li key={x}>{x}</li>)}</ol></article>
    <article className="analysis-card"><h3>必须条件</h3><TagList values={list('mustHaveRequirements')} /></article>
    <article className="analysis-card"><h3>加分条件</h3><TagList values={list('niceToHaveRequirements')} /></article>
    <article className="analysis-card"><h3>技术关键词</h3><TagList values={list('technicalKeywords')} accent /></article>
    <article className="analysis-card"><h3>简历优先级</h3><ul>{list('resumePriorities').map(x => <li key={x}>{x}</li>)}</ul></article>
  </div>
}

function TagList({ values, accent = false }: { values: string[]; accent?: boolean }) {
  return <div className="tag-list">{values.map(x => <span className={accent ? 'accent' : ''} key={x}>{x}</span>)}</div>
}

function RecommendationStep({ recommendations, projects, selected, setSelected, busy, onContinue }: {
  recommendations: Recommendation[]; projects: Project[]; selected: string[]; setSelected: (v: string[]) => void; busy: boolean; onContinue: () => void
}) {
  const choose = (index: number, id: string) => setSelected(selected.map((x, i) => i === index ? id : x))
  return <div className="panel">
    <div className="panel-heading"><div><p className="section-number">03 / EVIDENCE MATCH</p><h2>最匹配的三个项目</h2><p>推荐只是起点。你可以替换项目，但三个位置不能重复。</p></div><span className="status-chip">{recommendations[0]?.source === 'GEMINI' ? 'Gemini 重排' : '本地匹配'}</span></div>
    <div className="recommendation-grid">
      {[0,1,2].map(index => {
        const selectedProject = projects.find(p => p.id === selected[index])
        const rec = recommendations.find(r => r.project.id === selected[index]) ?? recommendations[index]
        return <article className="project-card" key={index}>
          <div className="rank-line"><span>0{index + 1}</span><b>{rec?.score ?? '—'} / 100</b></div>
          <select value={selected[index] ?? ''} onChange={e => choose(index, e.target.value)}>
            <option value="">选择项目</option>{projects.map(p => <option disabled={selected.includes(p.id) && selected[index] !== p.id} value={p.id} key={p.id}>{p.nameZh}</option>)}
          </select>
          {selectedProject && <><h3>{selectedProject.nameDe || selectedProject.nameZh}</h3><p>{selectedProject.summary}</p><TagList values={selectedProject.technologies.split(',').slice(0,6)} accent />{rec && <div className="match-note"><b>匹配理由</b><p>{rec.reason}</p>{rec.gaps.length > 0 && <small>缺口：{rec.gaps.join('；')}</small>}</div>}</>}
        </article>
      })}
    </div>
    <div className="action-row"><p>下一步会为每个项目生成独立 Codex Prompt。</p><button className="primary-button" disabled={busy || selected.length !== 3 || new Set(selected).size !== 3} onClick={onContinue}>{busy && <LoaderCircle className="spin" size={17}/>}确认三个项目</button></div>
  </div>
}

function DraftStep({ drafts, busy, onSave, onGenerate }: { drafts: Draft[]; busy: string; onSave: (position: number, latex: string, approve: boolean) => void; onGenerate: () => void }) {
  const [texts, setTexts] = useState<Record<number, string>>({})
  useEffect(() => setTexts(Object.fromEntries(drafts.map(d => [d.position, d.latex ?? '']))), [drafts])
  const allApproved = drafts.length === 3 && drafts.every(d => d.approved)
  return <div className="panel draft-panel">
    <div className="panel-heading"><div><p className="section-number">04 / HUMAN IN THE LOOP</p><h2>分别生成并核对项目描述</h2><p>复制每个 Prompt 到独立 Codex 窗口，再把结果粘贴回来。</p></div><span className="status-chip">{drafts.filter(d => d.approved).length} / 3 已确认</span></div>
    <div className="draft-stack">{drafts.map(d => <DraftEditor key={d.id} draft={d} value={texts[d.position] ?? ''} setValue={v => setTexts(t => ({ ...t, [d.position]: v }))} busy={busy === `draft-${d.position}`} onSave={onSave}/>)}</div>
    <div className="action-row"><p>只有三个项目均通过“恰好四条”校验后才能生成。</p><button className="primary-button" disabled={busy !== '' || !allApproved} onClick={onGenerate}>{busy === 'generate' && <LoaderCircle className="spin" size={17}/>}生成简历</button></div>
  </div>
}

function DraftEditor({ draft, value, setValue, busy, onSave }: { draft: Draft; value: string; setValue: (v: string) => void; busy: boolean; onSave: (position: number, latex: string, approve: boolean) => void }) {
  const [copied, setCopied] = useState(false)
  const copy = async () => { await navigator.clipboard.writeText(draft.prompt); setCopied(true); window.setTimeout(() => setCopied(false), 1500) }
  return <article className={`draft-editor ${draft.approved ? 'approved' : ''}`}>
    <div className="draft-title"><div><span>PROJECT 0{draft.position}</span><h3>{draft.project.nameDe || draft.project.nameZh}</h3></div>{draft.approved && <b><Check size={15}/>已确认</b>}</div>
    <details><summary>查看项目事实</summary><p>{draft.project.summary}</p><p><b>事实边界：</b>{draft.project.facts}</p></details>
    <button className="copy-button" onClick={copy}>{copied ? <Check size={16}/> : <Clipboard size={16}/>} {copied ? '已复制' : '复制 Codex Prompt'}</button>
    <label>粘贴 Codex 生成的 LaTeX<textarea className="latex-input" value={value} onChange={e => setValue(e.target.value)} placeholder="\resumeProjectHeading …" spellCheck={false}/></label>
    {draft.errors.length > 0 && <ul className="validation-list">{draft.errors.map(e => <li key={e}>{e}</li>)}</ul>}
    <div className="draft-actions"><button className="secondary-button" disabled={busy || !value.trim()} onClick={() => onSave(draft.position, value, false)}>保存并检查</button><button className="approve-button" disabled={busy || !value.trim()} onClick={() => onSave(draft.position, value, true)}>{busy && <LoaderCircle className="spin" size={15}/>}检查并确认</button></div>
  </article>
}

function ExportStep({ application, generation }: { application: Application; generation: Generation }) {
  return <div className="panel export-panel">
    <div className="success-orbit"><Check size={34}/></div>
    <p className="section-number">05 / READY TO APPLY</p>
    <h2>简历已经生成</h2>
    <p>{application.jobTitle}{application.companyName ? ` · ${application.companyName}` : ''}</p>
    {generation.errorMessage && <div className="inline-warning">{generation.errorMessage}</div>}
    <div className="download-grid">
      <a className="download-card" href={api.downloadUrl(application.id, generation.id, 'tex')}><FileText/><span><b>LaTeX 源文件</b><small>始终可用，可继续手工调整</small></span><Download/></a>
      {generation.status === 'PDF_READY' && <a className="download-card dark" href={api.downloadUrl(application.id, generation.id, 'pdf')}><FileText/><span><b>PDF 简历</b><small>已通过隔离编译生成</small></span><Download/></a>}
    </div>
  </div>
}

export default App
