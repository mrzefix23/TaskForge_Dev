import { ComponentFixture, TestBed } from '@angular/core/testing'
import { By } from '@angular/platform-browser'
import { DebugElement } from '@angular/core'
import { Kanban } from './kanban'

interface Task {
  id: number
  title: string
  description?: string
  status: string
  priority?: string
  assignee?: string
  createdAt?: Date
  updatedAt?: Date
}

describe('Kanban (Angular)', () => {
  let component: Kanban
  let fixture: ComponentFixture<Kanban>
  let debug: DebugElement

  const mockTasks: Task[] = [
    { id: 1, title: 'Task 1', status: 'todo', priority: 'high', assignee: 'user1', createdAt: new Date('2024-01-01'), updatedAt: new Date('2024-01-01') },
    { id: 2, title: 'Task 2', status: 'in-progress', priority: 'medium', assignee: 'user2', createdAt: new Date('2024-01-02'), updatedAt: new Date('2024-01-02') },
    { id: 3, title: 'Task 3', status: 'done', priority: 'low', assignee: 'user1', createdAt: new Date('2024-01-03'), updatedAt: new Date('2024-01-03') }
  ]

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [Kanban]
    }).compileComponents()

    fixture = TestBed.createComponent(Kanban)
    component = fixture.componentInstance
    debug = fixture.debugElement
    component.tasks = mockTasks as any
    fixture.detectChanges()
  })

  it('should create', () => {
    expect(component).toBeTruthy()
  })

  it('affiche les trois colonnes', () => {
    const todo = debug.query(By.css('[data-testid="column-todo"]'))
    const inProgress = debug.query(By.css('[data-testid="column-in-progress"]'))
    const done = debug.query(By.css('[data-testid="column-done"]'))

    expect(todo).toBeTruthy()
    expect(inProgress).toBeTruthy()
    expect(done).toBeTruthy()
  })

  it('affiche le nombre correct de tâches par colonne', () => {
    const todoColumn = debug.query(By.css('[data-testid="column-todo"]'))
    const inProgressColumn = debug.query(By.css('[data-testid="column-in-progress"]'))
    const doneColumn = debug.query(By.css('[data-testid="column-done"]'))

    const todoTasks = todoColumn.queryAll(By.css('[data-testid^="task-"]'))
    const inProgressTasks = inProgressColumn.queryAll(By.css('[data-testid^="task-"]'))
    const doneTasks = doneColumn.queryAll(By.css('[data-testid^="task-"]'))

    expect(todoTasks.length).toBe(1)
    expect(inProgressTasks.length).toBe(1)
    expect(doneTasks.length).toBe(1)
  })

  it('affiche les tâches dans les bonnes colonnes', () => {
    const todoColumn = debug.query(By.css('[data-testid="column-todo"]'))!.nativeElement as HTMLElement
    const inProgressColumn = debug.query(By.css('[data-testid="column-in-progress"]'))!.nativeElement as HTMLElement
    const doneColumn = debug.query(By.css('[data-testid="column-done"]'))!.nativeElement as HTMLElement

    expect(todoColumn.textContent).toContain('Task 1')
    expect(inProgressColumn.textContent).toContain('Task 2')
    expect(doneColumn.textContent).toContain('Task 3')
  })

  it('affiche un message quand une colonne est vide', () => {
    component.tasks = [mockTasks[0]] as any
    fixture.detectChanges()

    const inProgressColumn = debug.query(By.css('[data-testid="column-in-progress"]'))!.nativeElement as HTMLElement
    const doneColumn = debug.query(By.css('[data-testid="column-done"]'))!.nativeElement as HTMLElement

    expect(inProgressColumn.textContent).toContain('Aucune tâche')
    expect(doneColumn.textContent).toContain('Aucune tâche')
  })

  it('ajoute la classe de survol lors du dragover et la retire au dragleave', () => {
    const dropZone = debug.query(By.css('[data-testid="drop-zone-todo"]'))
    expect(dropZone).toBeTruthy()

    const el = dropZone!.nativeElement as HTMLElement
    el.dispatchEvent(new Event('dragover'))
    fixture.detectChanges()
    expect(el.classList.contains('drag-over')).toBeTrue()

    el.dispatchEvent(new Event('dragleave'))
    fixture.detectChanges()
    expect(el.classList.contains('drag-over')).toBeFalse()
  })

  it('met à jour les compteurs après filtrage basique (si contrôles présents)', () => {
    // Tentative non-invasive : si le composant expose un control ou un input data-testid="filter-priority", on simule
    const priorityInput = debug.query(By.css('[data-testid="filter-priority"]'))
    if (priorityInput) {
      const inputEl = priorityInput.nativeElement as HTMLInputElement
      inputEl.value = 'high'
      inputEl.dispatchEvent(new Event('input'))
      fixture.detectChanges()

      const countTodo = debug.query(By.css('[data-testid="count-todo"]'))!.nativeElement as HTMLElement
      const countInProgress = debug.query(By.css('[data-testid="count-in-progress"]'))!.nativeElement as HTMLElement
      const countDone = debug.query(By.css('[data-testid="count-done"]'))!.nativeElement as HTMLElement

      expect(countTodo.textContent?.trim()).toBe('1')
      expect(countInProgress.textContent?.trim()).toBe('0')
      expect(countDone.textContent?.trim()).toBe('0')
    } else {
      // si pas de contrôle de filtre dans le DOM, l'existence des compteurs reste vérifiée de base
      const countTodo = debug.query(By.css('[data-testid="count-todo"]'))
      expect(countTodo).toBeTruthy()
    }
  })
})
