import { Component, OnInit, Input, Output, EventEmitter, ViewChild} from '@angular/core';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {Tools} from '../../shared/tools';
import {MarkdownPipe} from '../../pipes/markdown.pipe';

declare var markdownitEmoji: any;
declare var autosize: any;

@Component({

  selector: 'app-markdown-edit',
  templateUrl: './markdown-edit.component.html',
  styleUrls: ['./markdown-edit.component.scss']
})
export class MarkdownEditComponent implements OnInit {

  @Output() textEvent = new EventEmitter();

  @Input() inputText: string;

  @Input() rows: number = 3;

  @Input() focus: boolean = true;

  @ViewChild('textArea') textArea;

  private textBox: string;
  private html: SafeHtml;

  private previewMode: boolean = false;

  constructor(private sanitizer: DomSanitizer) {}

  ngOnInit() {
    if (this.inputText != null) {
      this.textBox = this.inputText;
    }
  }

  ngAfterViewInit() {
    autosize(this.textArea.nativeElement);
    if (this.focus) {
      this.textArea.nativeElement.focus()
    };
  }

  setText() {
    this.textEvent.emit(this.textBox);
  }

  preview() {
    this.previewMode = !this.previewMode;
    this.html = new MarkdownPipe(this.sanitizer).transform(this.textBox);
  }

  bold() {
    this.textBox = this.surroundAtCursor(this.textArea.nativeElement, "**", "**");
    this.setText();
  }

  italics() {
    this.textBox = this.surroundAtCursor(this.textArea.nativeElement, "*", "*");
    this.setText();
  }

  header() {
    this.textBox = this.surroundAtCursor(this.textArea.nativeElement, "# ", "");
    this.setText();
  }

  link() {
    this.textBox = this.surroundAtCursor(this.textArea.nativeElement, "[", "](http://)");
    this.setText();
  }

  picture() {
    this.textBox = this.surroundAtCursor(this.textArea.nativeElement, "![", "](http://)");
    this.setText();
  }

  unorderedList() {
    this.textBox = this.surroundAtCursor(this.textArea.nativeElement, "- ", "");
    this.setText();
  }
  orderedList() {
    this.textBox = this.surroundAtCursor(this.textArea.nativeElement, "1. ", "");
    this.setText();
  }

  code() {
    this.textBox = this.surroundAtCursor(this.textArea.nativeElement, "```\n", "\n```");
    this.setText();
  }

  quote() {
    this.textBox = this.surroundAtCursor(this.textArea.nativeElement, "> ", "");
    this.setText();
  }


  private surroundAtCursor(myField, beforeMyValue, afterMyValue) {

    if (myField.selectionStart || myField.selectionStart == '0') {
      var startPos = myField.selectionStart;
      var endPos = myField.selectionEnd;
      var beforeSelection = myField.value.substring(0, startPos);
      var afterSelection = myField.value.substring(endPos, myField.value.length);
      var selection = myField.value.substring(startPos, endPos);

      myField.value = beforeSelection + beforeMyValue + selection +
        afterMyValue + afterSelection;
    }
    else {
      myField.value += beforeMyValue + afterMyValue;
    }
    myField.focus();
    return myField.value;
  }



}
