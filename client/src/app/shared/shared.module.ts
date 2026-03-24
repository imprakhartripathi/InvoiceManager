import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';

import { DynamicFieldComponent } from './components/dynamic-field/dynamic-field.component';
import { LineItemsEditorComponent } from './components/line-items-editor/line-items-editor.component';
import { MaterialModule } from './ui/material.module';

@NgModule({
  declarations: [DynamicFieldComponent, LineItemsEditorComponent],
  imports: [CommonModule, ReactiveFormsModule, MaterialModule],
  exports: [CommonModule, ReactiveFormsModule, MaterialModule, DynamicFieldComponent, LineItemsEditorComponent]
})
export class SharedModule {}
