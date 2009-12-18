/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.refactoring.ui.pages.rename;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.rename.IAmbiguousRenameInfo;
import org.codehaus.groovy.eclipse.refactoring.ui.GroovyRefactoringMessages;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class RenameFileSelectionPage extends UserInputWizardPage {

	private static final boolean TABLE_LINES_VISIBLE = false;
	private static final int WIDTH = 200;
	private static final String DOCUMENT = "document";
	private static final String ASTNODE = "astnode";
	
	private IAmbiguousRenameInfo info;
	private SourceViewer sourceCodePreviewViewer;
	private Table possibilityTable;
	private Table definitiveTable;

	public RenameFileSelectionPage(String name, IAmbiguousRenameInfo info) {
		super(name);
		this.info = info;
		setTitle(name);
		setMessage(name);
	}

	public void createControl(Composite parent) {
		Composite control = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout ();
		gridLayout.numColumns = 2;
		control.setLayout (gridLayout);
		
		initLabel(GroovyRefactoringMessages.RenameMethodFileSelectionPage_LB_AmbiguousCandidates, control);
		initLabel(GroovyRefactoringMessages.RenameMethodFileSelectionPage_LB_CodePreview, control);
		initSelectButtons(control);
		initSourceCodePreview(control);
		initPossibilityTable(control);
		initLabel(GroovyRefactoringMessages.RenameMethodFileSelectionPage_LB_DefinitveCandidates, control);
		initDefinitiveTable(control);
		
		setControl(control);
	}

	private void initSelectButtons(Composite control) {
		Composite buttons = new Composite(control, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		buttons.setLayout(gridLayout);
		
		GridData buttonData = new GridData();
		buttonData.widthHint = 90;

		Button selectAllButton = new Button (buttons, SWT.PUSH);
		selectAllButton.setText ("Select All");
		selectAllButton.setAlignment(SWT.CENTER);
		selectAllButton.addSelectionListener(new CheckAllInTableListener(true));
		selectAllButton.setLayoutData(buttonData);
		
		Button unselectAllButton = new Button (buttons, SWT.PUSH);
		unselectAllButton.setText ("Unselect All");
		unselectAllButton.setAlignment(SWT.CENTER);
		unselectAllButton.addSelectionListener(new CheckAllInTableListener(false));
		unselectAllButton.setLayoutData(buttonData);
	}

	private void initSourceCodePreview(Composite control) {
		GridData data;
		sourceCodePreviewViewer = createViewer(control);
		data = new GridData ();
		data.minimumWidth = WIDTH;
		data.widthHint = WIDTH;
		data.heightHint = 400;
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		data.verticalSpan = 4;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		sourceCodePreviewViewer.getTextWidget().setLayoutData(data);
	}
	
    protected SourceViewer createViewer(Composite parent) {
        SourceViewer viewer = new SourceViewer(parent, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
          
        IDocument document = new Document();       
        viewer.configure(new MinimalGroovyConfiguration());
        viewer.setDocument(document);
        viewer.setEditable(false);  
        Font font= JFaceResources.getFont(JFaceResources.TEXT_FONT);
        viewer.getTextWidget().setFont(font);    
                
        return viewer;
    }


	private void initPossibilityTable(Composite control) {
		possibilityTable = new Table (control, SWT.BORDER | SWT.CHECK);
		possibilityTable.setLinesVisible (TABLE_LINES_VISIBLE);
		TableColumn column = new TableColumn (possibilityTable, SWT.NONE);
		column.setWidth(WIDTH - 27);
		GridData data = new GridData ();
		data.widthHint = WIDTH;
		data.heightHint = 200;
		possibilityTable.setLayoutData (data);
		
		addTableEntries(info.getAmbiguousCandidates(), possibilityTable);
		possibilityTable.addSelectionListener(new TableSelectionListener(true));
	}

	private void addTableEntries(Map<IGroovyDocumentProvider, List<ASTNode>> ambiguousCandidates, Table table) {
		for (Entry<IGroovyDocumentProvider, List<ASTNode>> candid : ambiguousCandidates.entrySet()) {
			for (ASTNode nodeCandid : candid.getValue()) {
				TableItem currentItem = new TableItem(table, SWT.CHECK);
				int lineNumber = nodeCandid.getLineNumber();
				String tableLineText = "File: " + candid.getKey().getName() + " (Line: " + lineNumber + ")";
				currentItem.setText(tableLineText);
				currentItem.setData(ASTNODE, nodeCandid);
				currentItem.setData(DOCUMENT, candid.getKey());
			}
		}
	}
	
	private Label initLabel(String text, Composite parent){
		Label newLabel = new Label(parent, SWT.NONE);
		newLabel.setText(text);
		return newLabel;
	}

	private void initDefinitiveTable(Composite control) {
		GridData data = new GridData ();
		definitiveTable = new Table (control, SWT.BORDER);
		definitiveTable.setLinesVisible (TABLE_LINES_VISIBLE);
		TableColumn column = new TableColumn (definitiveTable, SWT.NONE);
		column.setWidth(WIDTH);
		data.widthHint = WIDTH;
		data.heightHint = 200;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessVerticalSpace = true;
		definitiveTable.setLayoutData (data);
		
		addTableEntries(info.getDefinitiveCandidates(), definitiveTable);
		definitiveTable.addSelectionListener(new TableSelectionListener(false));
	}

	private final class CheckAllInTableListener implements SelectionListener {
		
		private boolean checkAllBoxes;
		public CheckAllInTableListener(boolean check) {
			checkAllBoxes = check;
		}
		
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		public void widgetSelected(SelectionEvent e) {
			TableItem[] tableItems = possibilityTable.getItems();
			for(TableItem item : tableItems){
				item.setChecked(checkAllBoxes);
				ASTNode nodeOfTableElement = (ASTNode) item.getData(ASTNODE);
				if(item.getChecked()){
					info.addDefinitiveEntry((IGroovyDocumentProvider) item.getData(DOCUMENT),nodeOfTableElement);
				} else {
					info.removeDefinitiveEntry((IGroovyDocumentProvider) item.getData(DOCUMENT),nodeOfTableElement);
				}
			}
		}
		
	}

	private final class TableSelectionListener implements SelectionListener {
		
		private boolean selectableElements;
		
		public TableSelectionListener(boolean selectable) {
			selectableElements = selectable;
		}
		
		public void widgetDefaultSelected(SelectionEvent e) {
		}
		
		public void widgetSelected(SelectionEvent e) {
			handleTableItemClick(e);
		}

		private void handleTableItemClick(SelectionEvent e) {
			TableItem selectedTableItem = (TableItem) e.item;
			ASTNode nodeOfTableElement = (ASTNode) selectedTableItem.getData(ASTNODE);
			
			if(selectableElements){
				if(selectedTableItem.getChecked()){
					info.addDefinitiveEntry((IGroovyDocumentProvider) selectedTableItem.getData(DOCUMENT),nodeOfTableElement);
				} else {
					info.removeDefinitiveEntry((IGroovyDocumentProvider) selectedTableItem.getData(DOCUMENT),nodeOfTableElement);
				}
			}

			if(e.getSource().equals(definitiveTable)){
				possibilityTable.deselectAll();
			} else if(e.getSource().equals(possibilityTable)){
				definitiveTable.deselectAll();
			}
			
			IGroovyDocumentProvider documentOfTableElement = (IGroovyDocumentProvider) selectedTableItem.getData(DOCUMENT);
			UserSelection start = new UserSelection(nodeOfTableElement, documentOfTableElement.getDocument());
			sourceCodePreviewViewer.setDocument(documentOfTableElement.getDocument());
			sourceCodePreviewViewer.setSelection(new TextSelection(start.getOffset(), start.getOffset() + start.getLength()));
		}
	}

}
