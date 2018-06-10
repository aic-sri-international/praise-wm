// @flow

import ace from './aceBoot';

export type AceModelEditorArgs = {
  mode: string,
  value?: string,
  minLines?: number,
  maxLines?: number,
  showPrintMargin?: boolean,
  showGutter?: boolean,
  readOnly?: boolean,
}

export default class AceModelEditor {
  editor: Object;
  sessions: Object[] = [];
  editorContainerParent: Object;
  editorMode: string;

  constructor(args: AceModelEditorArgs) {
    const {
      value, minLines, maxLines, showPrintMargin, showGutter, mode, readOnly,
    } = args;
    this.editorMode = mode;

    this.editor = ace.edit(null, {
      minLines,
      maxLines,
      value: value || '',
      mode,
      showPrintMargin: showPrintMargin || false,
      showGutter: showGutter !== false,
      readOnly: readOnly === true,
    });
    if (this.editor.getReadOnly()) {
      this.editor.renderer.$cursorLayer.element.style.display = 'none';
    }
    const curSession = this.editor.getSession();
    AceModelEditor.configSession(curSession);
    this.sessions.push(curSession);
  }

  appendToRef(ref: Object) {
    this.editorContainerParent = ref;
    ref.appendChild(this.editor.container);
  }

  on(action: string, func: Function) {
    this.editor.on(action, func);
  }

  setValue(text: string) {
    this.editor.setValue(text);
    this.editor.moveCursorTo(0, 0);
  }

  getValue() {
    return this.editor.getValue();
  }

  clearSelection() {
    this.editor.clearSelection();
  }

  canUndo() {
    return this.editor.getSession().getUndoManager().canUndo();
  }

  canRedo() {
    return this.editor.getSession().getUndoManager().canRedo();
  }

  undo() {
    this.editor.getSession().getUndoManager().undo();
  }

  redo() {
    this.editor.getSession().getUndoManager().redo();
  }

  resetUndoManager() {
    this.editor.getSession().getUndoManager().reset();
  }

  static configSession(session: Object) {
    session.setNewLineMode('unix');
  }

  addSession(text: string) {
    const session = ace.createEditSession(text, this.editorMode);
    AceModelEditor.configSession(session);
    this.sessions.push(session);
  }

  getSessionData(index: number) : ?{ value: string } {
    if (index < this.sessions.length) {
      const session : Object = this.sessions[index];
      return {
        value: session.getValue(),
      };
    }
    return null;
  }

  setSession(newIndex: number) {
    this.editor.setSession(this.sessions[newIndex]);
  }

  destroy() {
    this.editorContainerParent.removeChild(this.editor.container);
    this.editor.destroy();
    this.sessions.forEach(session => session.destroy);
  }
}

