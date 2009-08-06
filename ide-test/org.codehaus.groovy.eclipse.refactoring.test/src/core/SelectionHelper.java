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
package core;

import java.util.HashMap;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.SourceCodePoint;

public class SelectionHelper {
	
	public static UserSelection getSelection(HashMap<String, String> prop,
			IGroovyDocumentProvider groovyDocumentProvider) throws Exception {
		
		String selectionType = prop.get("selectionType");
		
		if(selectionType != null) {
			return determineSelection(prop, groovyDocumentProvider, selectionType);
		}
        return new UserSelection(0,0);
	}

	private static UserSelection determineSelection(
			HashMap<String, String> prop, IGroovyDocumentProvider groovyDocumentProvider,
			String selectionType) throws Exception {

		if(selectionType.endsWith("points")) {
			return createPointSelection(prop, groovyDocumentProvider);
		}
		if(selectionType.endsWith("offset")) {
			return createOffsetSelection(prop);
		}
		throw new Exception("Wrong Selection in testfile");
	}

	private static UserSelection createOffsetSelection(HashMap<String, String> prop) throws Exception {
		try {
			int selectionOffset = Integer.parseInt(prop.get("selectionOffset"));
			int selectionLength = Integer.parseInt(prop.get("selectionLength"));
			return new UserSelection(selectionOffset,selectionLength);
		
		} catch (Exception e) {
			throw new Exception("Illegal offset + length properties",e);
		}
	}

	private static UserSelection createPointSelection(
			HashMap<String, String> prop, IGroovyDocumentProvider groovyDocumentProvider) throws Exception {
		try {
			int sl = Integer.parseInt(prop.get("startLine"));
			int sc = Integer.parseInt(prop.get("startColumn"));
			int el = Integer.parseInt(prop.get("endLine"));
			int ec = Integer.parseInt(prop.get("endColumn"));					
			SourceCodePoint sourceCodePointStart = new SourceCodePoint(sl,sc);
			SourceCodePoint sourceCodePointEnd = new SourceCodePoint(el,ec);
			return new UserSelection(sourceCodePointStart,sourceCodePointEnd,groovyDocumentProvider.getDocument());
		
		} catch (Exception e) {
			throw new Exception("Illegal start- and endpoint properties",e);
		}
	}
}
