import { CommonModule, TitleCasePipe } from '@angular/common';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Task, TaskComment, TaskStatus } from './types/task';

@Component({
  selector: 'app-kanban',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './kanban.html',
  styleUrls: ['./kanban.css'],
})
export class Kanban {
  tasks: Task[] = [
    { id: 1, title: 'Design layout', status: 'Open', description: 'Create wireframes for the dashboard UI', priority: 'High', sprintId: 1 ,deadline: '2025-11-15', peopleAssigned: [1, 2]},
    { id: 2, title: 'Implement backend', status: 'In Progress', description: 'Set up REST APIs for task management' , priority: 'Medium', sprintId: 1, deadline: '2025-11-20', peopleAssigned: [2]},
    { id: 3, title: 'Write unit tests', status: 'Done', description: 'Add test cases for authentication module' , priority: 'Low', sprintId: 2 , deadline: '2025-11-10', peopleAssigned: [3]},
    { id: 4, title: 'Create login screen', status: 'Open', description: 'Build responsive login form with validations' , priority: 'High', sprintId: 2, deadline: '2025-11-18'},
    { id: 5, title: 'Integrate database', status: 'In Progress', description: 'Connect to MongoDB and model schemas', priority: 'Medium', sprintId: 1 ,peopleAssigned: [1,3]},
    { id: 6, title: 'Setup CI/CD pipeline', status: 'Done', description: 'Deploy app automatically with GitHub Actions' , priority: 'Low', sprintId: 3 },
  ];

  members = [
    { id: 1, username: 'alice' },
    { id: 2, username: 'bob' },
    { id: 3, username: 'charlie' },
  ];

  // Filters
  selectedPriority: 'High' | 'Medium' | 'Low' | '' = '';
  selectedSprint: string = '';

  // task selected
  selectedTask: Task | null = null;

  // comment editing state
  editingCommentId: number | null = null;

  // new comment content
  newCommentContent: string = '';

  // show/hide members dropdown
  showPriorityDropdown = false;

  // show/hide deadline picker
  showDeadlinePicker = false;

  // show/hide members dropdown
  showMembersDropdown = false;

  // Get tasks by status with applied filters
  getTaskByStatus(status: string) {
    return this.tasks
      .filter(t => t.status === status)
      .filter(t => !this.selectedPriority || t.priority === this.selectedPriority)
      .filter(t => !this.selectedSprint || t.sprintId === Number(this.selectedSprint));
  }

  // columns configuration (id is the status value stored on tasks, label is the editable name)
  columns: Array<{ id: string; label: string }> = [
    { id: 'Open', label: 'Open' },
    { id: 'In Progress', label: 'In Progress' },
    { id: 'Done', label: 'Done' },
  ];

  // column edit state
  editingColumnId: string | null = null;
  editedColumnLabel = '';

  // Editing state
  editingTaskId: number | null = null;
  editedDescription = '';
  // context menu state
  contextMenuVisible = false;
  contextMenuX = 0;
  contextMenuY = 0;
  contextMenuTaskId: number | null = null;
  // column dragging state
  draggingColumnId: string | null = null;

  startEdit(task: Task) {
    this.editingTaskId = task.id;
    // we only edit the description now (no separate title)
    this.editedDescription = task.description;
    // focus the single-line input after the view updates
    setTimeout(() => {
      const el = document.getElementById(`edit-input-${task.id}`) as HTMLInputElement | null;
      if (el) {
        el.focus();
        el.select();
      }
    }, 0);
  }

  // column operations
  startEditColumn(col: { id: string; label: string }) {
    this.editingColumnId = col.id;
    this.editedColumnLabel = col.label;
    setTimeout(() => {
      const safeId = col.id.replace(/\s+/g, '-');
      const el = document.getElementById(`col-input-${safeId}`) as HTMLInputElement | null;
      if (el) {
        el.focus();
        el.select();
      }
    }, 0);
  }

  saveColumn(col: { id: string; label: string }) {
    if (!this.editingColumnId) return;
    const c = this.columns.find(x => x.id === col.id);
    if (!c) return;
    c.label = this.editedColumnLabel.trim() || c.label;
    this.editingColumnId = null;
  }

  cancelEditColumn() {
    this.editingColumnId = null;
  }

  addColumn() {
    const id = `col-${Date.now()}`;
    const newCol = { id, label: 'Nouvelle colonne' };
    this.columns.push(newCol);
    // open for editing
    setTimeout(() => this.startEditColumn(newCol), 0);
  }

  getSafeId(id: string) {
    return id.replace(/\s+/g, '-').replace(/[^a-zA-Z0-9-_]/g, '');
  }

  openContextMenu(event: MouseEvent, task: Task) {
    event.preventDefault();
    this.contextMenuTaskId = task.id;
    this.contextMenuX = event.clientX;
    this.contextMenuY = event.clientY;
    this.contextMenuVisible = true;
  }

  setPriority(task: Task, priority: 'Low' | 'Medium' | 'High') {
    const t = this.tasks.find(x => x.id === task.id);
    if (!t) return;
    t.priority = priority;
    this.contextMenuVisible = false;
  }

  deleteTask(task: Task) {
    this.tasks = this.tasks.filter(t => t.id !== task.id);
    this.contextMenuVisible = false;
    // if currently editing this task, cancel edit
    if (this.editingTaskId === task.id) this.cancelEdit();
  }

  // safer helpers that take an id (used by the template)
  setPriorityById(taskId: number | null, priority: 'Low' | 'Medium' | 'High') {
    if (taskId == null) return;
    const t = this.tasks.find(x => x.id === taskId);
    if (!t) return;
    t.priority = priority;
    this.closeContextMenu();
  }

  deleteTaskById(taskId: number | null) {
    if (taskId == null) return;
    this.tasks = this.tasks.filter(t => t.id !== taskId);
    if (this.editingTaskId === taskId) this.cancelEdit();
    this.closeContextMenu();
  }

  closeContextMenu() {
    this.contextMenuVisible = false;
    this.contextMenuTaskId = null;
  }

  private onWindowClick = (e: MouseEvent) => {
    if (this.contextMenuVisible) this.closeContextMenu();
  }

  private onKeyDown = (e: KeyboardEvent) => {
    if (e.key === 'Escape' && this.contextMenuVisible) this.closeContextMenu();
  }

  ngOnInit(): void {
    window.addEventListener('click', this.onWindowClick);
    window.addEventListener('keydown', this.onKeyDown);
  }

  ngOnDestroy(): void {
    window.removeEventListener('click', this.onWindowClick);
    window.removeEventListener('keydown', this.onKeyDown);
  }

  addTask(status: string) {
    // find max id
    const maxId = this.tasks.reduce((m, t) => Math.max(m, t.id), 0);
    const newTask: Task = {
      id: maxId + 1,
      title: '',
      status: status as TaskStatus,
      description: ''
    };
    this.tasks.unshift(newTask);
    // open the new task for editing in the column where it was added
    this.startEdit(newTask);
  }

  // column drag handlers
  onColumnDragStart(event: DragEvent, col: { id: string; label: string }) {
    event.dataTransfer?.setData('text', `col:${col.id}`);
    this.draggingColumnId = col.id;
  }

  onColumnDragEnd(event: DragEvent) {
    this.draggingColumnId = null;
  }

  saveEdit(task: Task) {
    if (!this.editingTaskId) return;
    const t = this.tasks.find(x => x.id === task.id);
    if (!t) return;
    t.description = this.editedDescription.trim();
    this.editingTaskId = null;
  }

  cancelEdit() {
    this.editingTaskId = null;
  }

  onDrop(event: DragEvent, status: string) {
    event.preventDefault();
    const data = event.dataTransfer?.getData('text') || '';
    if (data.startsWith('col:')) {
      const sourceId = data.slice(4);
      const targetId = status;
      if (sourceId === targetId) return;
      const srcIndex = this.columns.findIndex(c => c.id === sourceId);
      const tgtIndex = this.columns.findIndex(c => c.id === targetId);
      if (srcIndex === -1 || tgtIndex === -1) return;
      const [col] = this.columns.splice(srcIndex, 1);
      // insert at target index
      this.columns.splice(tgtIndex, 0, col);
      this.draggingColumnId = null;
      return;
    }

    // otherwise assume task move
    const taskId = Number(data || event.dataTransfer?.getData("text"));
    const task = this.tasks.find(t => t.id === taskId);
    if (task) {
      task.status = status as TaskStatus;
    }

    const draggingEl = document.querySelector(".dragging");
    if (draggingEl) draggingEl.classList.remove("dragging");
  }

  onDragStart(event: DragEvent, task: Task) {
    // prevent dragging when editing this task
    if (this.editingTaskId !== null && this.editingTaskId === task.id) {
      event.preventDefault();
      return;
    }
    event.dataTransfer?.setData("text", task.id.toString());
    (event.target as HTMLElement).classList.add("dragging");
  }

  onDragEnd(event: DragEvent) {
    (event.target as HTMLElement).classList.remove("dragging");
  }

  // task details panel
  openTaskDetails(task: Task) {
    this.selectedTask = task; 
    this.closeContextMenu();

    this.showMembersDropdown = false;
    this.showDeadlinePicker = false;
    this.showPriorityDropdown = false;
  }


  // close task details panel
  closeTaskDetails() {
    this.selectedTask = null;
  }

  // save task details from panel
  saveTaskDetails() {
    if (!this.selectedTask) return;
    const t = this.tasks.find(x => x.id === this.selectedTask!.id);
    if (t) {
      Object.assign(t, this.selectedTask);
    }
    this.selectedTask = null;
  }

  // get column label by status
  getColumnLabel(status: string): string {
    const col = this.columns.find(c => c.id === status);
    return col ? col.label : status;
  }

  // get member username by id
  getMemberUsername(memberId: number): string {
    const member = this.members.find(m => m.id === memberId);
    return member ? member.username : 'Unknown';
  }

  // get assigned members as string
  getAssignedMembers(task: Task): string {
    if (!task.peopleAssigned || task.peopleAssigned.length === 0) {
      return 'None';
    }
    return task.peopleAssigned.map(id => this.getMemberUsername(id)).join(', ');
  }

  // edit comment
  editComment(comment: TaskComment) {
    const newContent = prompt('Edit comment:', comment.content);
    if (newContent !== null) {
      comment.content = newContent;
    }
  }

  // delete comment
  deleteComment(comment: TaskComment, task?: Task) {
    if (!task || !task.comments) return;
    task.comments = task.comments.filter(c => c.id !== comment.id);
  }

  // show/hide members dropdown
  toggleMembersDropdown() {
    this.showMembersDropdown = !this.showMembersDropdown;
  }

  // assign/unassign member
  toggleMember(task: Task | null, memberId: number) {
    if (!task) return;
    if (!task.peopleAssigned) {
      task.peopleAssigned = [];
    }
    const index = task.peopleAssigned.indexOf(memberId);
    if (index === -1) {
      task.peopleAssigned.push(memberId);
    } else {
      task.peopleAssigned.splice(index, 1);
    }
  }

  // show/hide deadline picker
  toggleDeadlinePicker() {
    this.showDeadlinePicker = !this.showDeadlinePicker;
  }

  // save deadline
  saveDeadline(task: Task | null, deadline: string) {
    if (!task) return;
    task.deadline = deadline;
    this.showDeadlinePicker = false;
  }

  // show/hide priority dropdown
  togglePriorityDropdown() {
    this.showPriorityDropdown = !this.showPriorityDropdown;
  }

  // check if member is assigned to task
  isMemberAssigned(task: Task | null, memberId: number): boolean {
    if (!task || !task.peopleAssigned) return false;
    return task.peopleAssigned.includes(memberId);
  }

  // update task description
  updateTaskDescription(newDesc: string) {
    if (!this.selectedTask) return;
    this.selectedTask.description = newDesc;

    const t = this.tasks.find(task => task.id === this.selectedTask!.id);
    if (t) {
      t.description = newDesc; 
    }
  }

  // add a new comment
  addComment() {
    if (!this.selectedTask || !this.newCommentContent.trim()) return;

    const newComment: TaskComment = {
      id: Date.now(), 
      authorId: 1, 
      content: this.newCommentContent.trim(),
      date: new Date().toISOString(),
      taskId: this.selectedTask.id
    };

    if (!this.selectedTask.comments) {
      this.selectedTask.comments = [];
    }

    this.selectedTask.comments.push(newComment);
    this.newCommentContent = ''; 
  }

  // start editing a comment
  startEditingComment(commentId: number) {
    this.editingCommentId = commentId;
  }

  // Save edited comment
  saveComment(comment: TaskComment) {
    this.editingCommentId = null;
  }



}
