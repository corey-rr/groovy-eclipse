 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.types;

import static org.codehaus.groovy.eclipse.core.util.ListUtil.newList;
import static org.codehaus.groovy.eclipse.core.util.MapUtil.newMap;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Abstract implementation. Derived types simply need to override one or both methods:<br>
 * {@link #collectAllFields(String)}<br>
 * {@link #collectAllMethods(String)}<br>
 * 
 * @author empovazan
 */
public abstract class AbstractMemberLookup implements IMemberLookup {
	public Field[] lookupFields(String type, String prefix, boolean accessible, boolean staticAccess, boolean exact) {
		List< Field > fields = collectAllFields(type);
		if (accessible) {
			removeInaccessibleTypes(fields, type, staticAccess);
		}
		
		removeNonPrefixed(fields, prefix, exact);
		
		if (staticAccess) {
			removeInstanceTypes(fields);
		}

		if (accessible) {
			removeShadowedTypes(fields);
		}
		return fields.toArray( new Field[ 0 ] );
	}
	
	public Property[] lookupProperties(String type, String prefix, boolean accessible, boolean staticAccess, boolean exact) {
		List< Property > properties = collectAllProperties(type);
		
		if (accessible) {
			removeInaccessibleTypes(properties, type, staticAccess);
		}
		
		if (staticAccess) {
			removeInstanceTypes(properties);
		}
		
		removeNonPrefixed(properties, prefix, exact);

		if (accessible) {
			removeShadowedTypes(properties);
		}
		return properties.toArray( new Property[ 0 ] );
	}
	
	private void removeNonPrefixed(List types, String prefix, boolean exact) {
		for (Iterator iter = types.iterator(); iter.hasNext(); ) {
			GroovyDeclaration type = (GroovyDeclaration) iter.next();
			
			if (exact) {
			    if (!prefix.equals(type.getName())) {
			        iter.remove();
			    }
			} else {
    			if (!TypeUtil.looselyMatches(prefix, type.getName())) {
    				iter.remove();
    			}
			}
		}
	}
	
	private List< Property > collectPropertiesFromMethods(List< Method > methods) {
		Map<String, Property> mapNameToProperty = newMap();

		for (Iterator iter = methods.iterator(); iter.hasNext();) {
			Method method = (Method) iter.next();
			String name = method.getName();
			if (name.startsWith("set") && name.length() > 3) {
				if (Character.isUpperCase(name.charAt(3))) {
					name = Character.toLowerCase(name.charAt(3)) + name.substring(4);
					addToNameToPropertyMapping(mapNameToProperty, name, method, false, true);
				}
			} else if (((name.startsWith("is") && name.length() > 2) || (name.startsWith("get") && name.length() > 3))
					&& method.getParameters().length == 0) {
				int ix = name.startsWith("is") ? 2 : 3;
				if (Character.isUpperCase(name.charAt(ix))) {
					name = Character.toLowerCase(name.charAt(ix)) + name.substring(ix + 1);
					addToNameToPropertyMapping(mapNameToProperty, name, method, true, false);
				}
			}
		}
		return newList( mapNameToProperty.values() );
	}

	/**
	 * @param mapNameToProperty
	 *            Mapping from property names to properties.
	 * @param propertyName
	 * @param method
	 *            The method used to extract return type, modifiers.
	 * @param readable
	 *            The initial readable state if this property is not in the mapping.
	 * @param writable
	 *            The initial writable state if this property is not in the mapping.
	 */
	private void addToNameToPropertyMapping( final Map< String, Property > mapNameToProperty, 
	                                         final String propertyName, 
	                                         final Method method,
	                                         final boolean readable, 
	                                         final boolean writable ) {
		//TODO: emp - need inferred to be set if appropriate.
		Property property = mapNameToProperty.get(propertyName);
		String returnType;

		if (property != null && property.isReadable()) {
			returnType = property.getSignature(); // Use previous getters return type.
		} else if (writable) {
			Parameter[] parameters = method.getParameters();
			if (parameters.length != 1) {
				return; // Nothing to do, this is not a setter we can deal with here.
			}
			returnType = parameters[0].getSignature();
		} else {
			returnType = method.getReturnType();
		}

		if (property == null) {
			property = new Property(method.getReturnType(), method.getModifiers(), propertyName, readable,
					writable, method.getDeclaringClass());
			mapNameToProperty.put(propertyName, property);
		} else if (readable) {
			// FUTURE: emp - should modifiers be separate for read/write? Yes. But doesn't matter for now.
			int modifiers = property.getModifiers();
			property = new Property(returnType, property.getModifiers() | modifiers, propertyName, true, property
					.isWritable(), property.getDeclaringClass());

			mapNameToProperty.put(propertyName, property);
		} else { // writable
			int modifiers = property.getModifiers();
			property = new Property(returnType, property.getModifiers() | modifiers, propertyName, property
					.isWritable(), true, property.getDeclaringClass());
			mapNameToProperty.put(propertyName, property);
		}
	}

	public Method[] lookupMethods(String type, String prefix, boolean accessible, boolean staticAccess, boolean exact) {
		List< Method > methods = collectAllMethods(type);
		
		removeNonPrefixed(methods, prefix, exact);
		
		if (accessible) {
			removeInaccessibleTypes(methods, type, staticAccess);
		}
		
		if (staticAccess) {
			removeInstanceTypes(methods);
		}
		
		if (accessible) {
			removeShadowedTypes(methods);
		}
		return methods.toArray(new Method[0]);
	}

	public Method[] lookupMethods(final String type, final String prefix,
            final String[] paramTypes, final boolean accessible,
            final boolean staticAccess, final boolean exact) {
		List< Method > methods = collectAllMethods(type);
		
		removeNonPrefixed(methods, prefix, exact);
		
		if (accessible) {
			removeInaccessibleTypes(methods, type, staticAccess);
		}
		if (staticAccess) {
			removeInstanceTypes(methods);
		}
		// TODO: filtering by parameter types.
		return methods.toArray( new Method[ 0 ] );
	}
	
	protected List< Field > collectAllFields(String type) {
		return newList( new Field[ 0 ] );
	}
	
	protected List< Property > collectAllProperties(String type) {
		return collectPropertiesFromMethods(collectAllMethods(type));
	}
	
	protected List< Method > collectAllMethods(String type) {
		return newList( new Method[ 0 ] );
	}
	
	protected void removeTypesByAnyModifier(List< GroovyDeclaration > types, int modifiers) {
		for (Iterator< GroovyDeclaration > iter = types.iterator(); iter.hasNext(); ) {
			GroovyDeclaration type = iter.next();
			if ((type.modifiers & modifiers) != 0) {
				iter.remove();
			}
		}
	}
	
	protected void removeTypesByExactModifiers(List< GroovyDeclaration > types, int modifiers) {
		for (Iterator< GroovyDeclaration > iter = types.iterator(); iter.hasNext(); ) {
			GroovyDeclaration type = iter.next();
			if ((type.modifiers & modifiers) != modifiers) {
				iter.remove();
			}
		}
	}
	
	protected void removeInstanceTypes(List< ? extends GroovyDeclaration > types) {
		for (Iterator< ? extends GroovyDeclaration > iter = types.iterator(); iter.hasNext(); ) {
			GroovyDeclaration type = iter.next();
			if ((type.modifiers & Modifiers.ACC_STATIC) == 0) {
				iter.remove();
			}
		}
	}
	
	protected void removeStaticTypes(List< GroovyDeclaration > types) {
		removeTypesByAnyModifier(types, Modifiers.ACC_STATIC);
	}
	
	protected void removeInaccessibleTypes(List types, String baseType, boolean staticAccess) {
		int flags = staticAccess ? Modifiers.ACC_PUBLIC : Modifiers.ACC_PUBLIC | Modifiers.ACC_PROTECTED;
		for (Iterator iter = types.iterator(); iter.hasNext(); ) {
			Member member = (Member) iter.next();
			if (!member.getDeclaringClass().getName().equals(baseType)) {
				// FUTURE: emp - This remove package access - this is not a good solution, and will have to be addressed
				// in the future.
				if ((member.modifiers & Modifiers.ACC_PRIVATE) != 0 || (member.modifiers & flags) == 0) {
					iter.remove();
				} else if ((member.modifiers & flags) == 0) {
					iter.remove();
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
    protected void removeShadowedTypes(List< ? extends GroovyDeclaration > types) {
		if (types.size() > 0 && types.get(0) instanceof Method) {
			removeShadowedMethods(types);
		} else {
			// Sort - results are by name and hiearchy.
			Collections.sort(types);
			for (int i = 1; i < types.size(); ++i) {
				GroovyDeclaration last = types.get(i - 1);
				GroovyDeclaration current = types.get(i);
				if (last.name.equals(current.name)) {
					types.remove(i-- - 1);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
    private void removeShadowedMethods(List< ? extends GroovyDeclaration > types) {
		Collections.sort(types);
		for (int i = 1; i < types.size(); ++i) {
			Method last = (Method) types.get(i - 1);
			Method current = (Method) types.get(i);
			if (last.name.equals(current.name) && areEqualParameterTypes(last, current)) {
				types.remove(i-- - 1);
			}
		}
	}

	private boolean areEqualParameterTypes(Method method1, Method method2) {
		Parameter[] info1 = method1.getParameters();
		Parameter[] info2 = method2.getParameters();
		
		if (info1.length != info2.length) {
			return false;
		}

		for (int i = 0; i < info1.length; ++i) {
			if (!info1[i].getSignature().equals(info2[i].getSignature())) {
				return false;
			}
		}

		return true;
	}

}