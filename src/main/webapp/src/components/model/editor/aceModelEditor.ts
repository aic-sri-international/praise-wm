// eslint-disable-next-line import/no-duplicates
import { Ace } from 'ace-builds';
// @ts-ignore
// eslint-disable-next-line import/no-duplicates
import ace from 'ace-builds/src-noconflict/ace';
import 'ace-builds/webpack-resolver';
import 'ace-builds/src-noconflict/mode-javascript';
import './mode-hogm';


export type AceModelEditorArgs = {
  mode: string;
  value?: string;
  minLines?: number;
  maxLines?: number;
  showPrintMargin?: boolean;
  showGutter?: boolean;
  readOnly?: boolean;
  highlightActiveLine?: boolean;
  styleClass?: string;
}

export default class AceModelEditor {
  editor: Ace.Editor;

  sessions: any[] = [];

  editorContainerParent: any;

  editorMode: string;

  constructor(args: AceModelEditorArgs) {
    const {
      value, minLines, maxLines, showPrintMargin, showGutter, mode, readOnly,
      highlightActiveLine, styleClass,
    } = args;
    this.editorMode = mode;

    this.editor = ace.edit(null, {
      minLines,
      maxLines,
      value: value || '',
      mode,
      showPrintMargin: showPrintMargin || false,
      showGutter,
      readOnly,
      highlightActiveLine,
    });

    if (styleClass) {
      this.editor.setStyle(styleClass);
    }

    // Delegate commands to the browser, so that a tab or shift-tab
    // navigate to the next UI component
    // @ts-ignore
    this.editor.commands.bindKey('Tab', null);
    // @ts-ignore
    this.editor.commands.bindKey('Shift-Tab', null);

    if (this.editor.getReadOnly()) {
      // @ts-ignore
      this.editor.renderer.$cursorLayer.element.style.display = 'none';
    }
    const curSession = this.editor.getSession();
    AceModelEditor.configSession(curSession);
    this.sessions.push(curSession);
  }

  static configSession(session: any) {
    session.setNewLineMode('unix');
  }

  appendToRef(ref: any) {
    this.editorContainerParent = ref;
    ref.appendChild(this.editor.container);
  }

  onChange(callback: (delta: Ace.Delta) => void) {
    const actionType = 'change';
    this.editor.on(actionType, callback);
  }

  setValue(text: string) {
    this.editor.setValue(text);
    this.editor.moveCursorTo(0, 0);
  }

  getValue() {
    return this.editor.getValue();
  }

  focus() {
    this.editor.focus();
  }

  setReadOnly(readOnly: boolean) {
    this.editor.setReadOnly(readOnly);
  }

  getReadOnly() {
    this.editor.getReadOnly();
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
    this.editor.getSession().getUndoManager().undo(this.editor.getSession());
  }

  redo() {
    this.editor.getSession().getUndoManager().redo(this.editor.getSession());
  }

  resetUndoManager() {
    this.editor.getSession().getUndoManager().reset();
  }

  addSession(text: string) {
    const session = ace.createEditSession(text, this.editorMode);
    AceModelEditor.configSession(session);
    this.sessions.push(session);
  }

  getSessionData(index: number): { value: string } | null {
    if (index < this.sessions.length) {
      const session: any = this.sessions[index];
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
