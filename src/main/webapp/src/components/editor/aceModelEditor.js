// @flow

import ace from './aceBoot';
import { segmentModel } from './modelSplitter';
import type { SegmentedModel, ModelRule } from './modelSplitter';


export type AceModelEditorArgs = {
  mode: string,
  value?: string,
  minLines?: number,
  maxLines?: number,
  showPrintMargin?: boolean,
  showGutter?: boolean,
  readOnly?: boolean,
}

type SessionEntry = {
  session: Object,
  metadata: string,
}

export default class AceModelEditor {
  editor: Object;
  sessionEntries: SessionEntry[] = [];
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
    this.sessionEntries.push({ session: curSession, metadata: '' });
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

  loadSegmentedModel(model: string) {
    const sm : SegmentedModel = segmentModel(model);
    if (!sm.rules) {
      throw Error('Model does not have any rules');
    }
    this.sessionEntries.forEach(e => e.session.destroy);
    this.sessionEntries = [];


    sm.rules.forEach((mr: ModelRule) => {
      this.addSession(mr.rule, mr.metadata);
    });

    this.editor.setSession(this.sessionEntries[0].session);
  }

  static configSession(session: Object) {
    session.setNewLineMode('unix');
  }

  addSession(text: string, metadata: ?string) {
    const session = ace.createEditSession(text, this.editorMode);
    AceModelEditor.configSession(session);
    this.sessionEntries.push({ session, metadata: metadata || '' });
  }

  getSessionData(index: number) : ?{ value: string, metadata: string } {
    if (index < this.sessionEntries.length) {
      const entry : SessionEntry = this.sessionEntries[index];
      return {
        value: entry.session.getValue(),
        metadata: entry.metadata,
      };
    }
    return null;
  }

  setSession(newIndex: number) {
    this.editor.setSession(this.sessionEntries[newIndex].session);
  }

  destroy() {
    this.editorContainerParent.removeChild(this.editor.container);
    this.editor.destroy();
    this.sessionEntries.forEach(e => e.session.destroy);
  }
}

