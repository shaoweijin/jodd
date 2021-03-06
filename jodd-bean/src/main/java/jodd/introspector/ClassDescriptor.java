// Copyright (c) 2003-2013, Jodd Team (jodd.org). All Rights Reserved.

package jodd.introspector;

import jodd.util.ReflectUtil;

import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.Collection;

/**
 * A descriptor class for all methods/fields/properties/constructors of a class.
 * Static methods/fields are ignored.
 * <p>
 * Descriptors are 'lazy': various internal caches are created on first request.
 * <p>
 * Throughout this class, public members are defined as members
 * defined with "public" keyword and declared in a public type.
 * Public members declared by a non-public class is considered non-public
 * because access to it from outside is prohibited by the java access control
 * anyway.
 * <p>
 * Public members defined in public classes are always preferred even
 * when we allow private/protected members and types to be visible.
 * So if a non-public subtype and a public super type both have a field
 * with the same name, the field in the public super type is always used.
 */
public class ClassDescriptor {

	protected final Class type;
	protected final boolean scanAccessible;
	protected final boolean extendedProperties;
	protected final boolean includeFieldsAsProperties;
	protected final String propertyFieldPrefix;
	protected int usageCount;

	public ClassDescriptor(Class type, boolean scanAccessible, boolean extendedProperties, boolean includeFieldsAsProperties, String propertyFieldPrefix) {
		this.type = type;
		this.scanAccessible = scanAccessible;
		this.extendedProperties = extendedProperties;
		this.includeFieldsAsProperties = includeFieldsAsProperties;
		this.propertyFieldPrefix = propertyFieldPrefix;

		isArray = type.isArray();
		isMap = ReflectUtil.isSubclass(type, Map.class);
		isList = ReflectUtil.isSubclass(type, List.class);
		isSet = ReflectUtil.isSubclass(type, Set.class);
		isCollection = ReflectUtil.isSubclass(type, Collection.class);
	}

	/**
	 * Get the class object that this descriptor describes.
	 */
	public Class getType() {
		return type;
	}

	/**
	 * Returns <code>true</code> if this class descriptor
	 * works with accessible fields/methods/constructors or with
	 * all supported.
	 */
	public boolean isScanAccessible() {
		return scanAccessible;
	}

	/**
	 * Returns <code>true</code> if properties in this class descriptor
	 * are extended and include field description.
	 */
	public boolean isExtendedProperties() {
		return extendedProperties;
	}

	/**
	 * Include fields as properties.
	 */
	public boolean isIncludeFieldsAsProperties() {
		return includeFieldsAsProperties;
	}

	/**
	 * Returns property field prefix. May be <code>null</code>
	 * if prefix is not set.
	 */
	public String getPropertyFieldPrefix() {
		return propertyFieldPrefix;
	}

	/**
	 * Increases usage count.
	 */
	protected void increaseUsageCount() {
		usageCount++;
	}

	/**
	 * Returns number of class descriptor usages. That is number
	 * of times when class descriptor for some class has been
	 * lookuped. Higher usage count means that some class is
	 * more frequently being used.
	 */
	public int getUsageCount() {
		return usageCount;
	}

	// ---------------------------------------------------------------- special

	private final boolean isArray;
	/**
	 * Returns <code>true</code> if class is an array.
	 */
	public boolean isArray() {
		return isArray;
	}

	private final boolean isMap;
	/**
	 * Returns <code>true</code> if class is a <code>Map</code>.
	 */
	public boolean isMap() {
		return isMap;
	}

	private final boolean isList;
	/**
	 * Returns <code>true</code> if class is a <code>List</code>.
	 */
	public boolean isList() {
		return isList;
	}

	private final boolean isSet;
	/**
	 * Returns <code>true</code> if type is a <code>Set</code>.
	 */
	public boolean isSet() {
		return isSet;
	}

	private final boolean isCollection;
	/**
	 * Returns <code>true</code> if type is a collection.
	 */
	public boolean isCollection() {
		return isCollection;
	}

	// ---------------------------------------------------------------- fields

	private Fields fields;

	/**
	 * Returns {@link Fields fields collection}.
	 * Creates new fields collection on first usage.
	 */
	protected Fields getFields() {
		if (fields == null) {
			fields = new Fields(this);
		}
		return fields;
	}

	/**
	 * Returns field descriptor.
	 */
	public FieldDescriptor getFieldDescriptor(String name, boolean declared) {
		FieldDescriptor fieldDescriptor = getFields().getFieldDescriptor(name);

		if (fieldDescriptor != null) {
			if (!fieldDescriptor.matchDeclared(declared)) {
				return null;
			}
		}

		return fieldDescriptor;
	}

	/**
	 * Returns all field descriptors, including declared ones.
	 */
	public FieldDescriptor[] getAllFieldDescriptors() {
		return getFields().getAllFieldDescriptors();
	}

	// ---------------------------------------------------------------- methods

	private Methods methods;

	/**
	 * Returns methods collection.
	 * Creates new collection on first access.
	 */
	protected Methods getMethods() {
		if (methods == null) {
			methods = new Methods(this);
		}
		return methods;
	}

	/**
	 * Returns {@link MethodDescriptor method descriptor} identified by name and parameters.
	 */
	public MethodDescriptor getMethodDescriptor(String name, boolean declared) {
		MethodDescriptor methodDescriptor = getMethods().getMethodDescriptor(name);

		if ((methodDescriptor != null) && methodDescriptor.matchDeclared(declared)) {
			return methodDescriptor;
		}

		return methodDescriptor;
	}


	/**
	 * Returns {@link MethodDescriptor method descriptor} identified by name and parameters.
	 */
	public MethodDescriptor getMethodDescriptor(String name, Class[] params, boolean declared) {
		MethodDescriptor methodDescriptor = getMethods().getMethodDescriptor(name, params);

		if ((methodDescriptor != null) && methodDescriptor.matchDeclared(declared)) {
			return methodDescriptor;
		}

		return null;
	}

	/**
	 * Returns an array of all methods with the same name.
	 */
	public MethodDescriptor[] getAllMethodDescriptors(String name) {
		return getMethods().getAllMethodDescriptors(name);
	}

	/**
	 * Returns an array of all methods.
	 */
	public MethodDescriptor[] getAllMethodDescriptors() {
		return getMethods().getAllMethodDescriptors();
	}

	// ---------------------------------------------------------------- properties

	private Properties properties;

	/**
	 * Returns properties collection.
	 * Creates new collection on first access.
	 */
	protected Properties getProperties() {
		if (properties == null) {
			properties = new Properties(this);
		}
		return properties;
	}

	/**
	 * Returns property descriptor. Declared flag is matched on both read and write
	 * methods.
	 */
	public PropertyDescriptor getPropertyDescriptor(String name, boolean declared) {
		PropertyDescriptor propertyDescriptor = getProperties().getPropertyDescriptor(name);

		if ((propertyDescriptor != null) && propertyDescriptor.matchDeclared(declared)) {
			return propertyDescriptor;
		}

		return null;
	}

	/**
	 * Returns all properties descriptors.
	 */
	public PropertyDescriptor[] getAllPropertyDescriptors() {
		return getProperties().getAllPropertyDescriptors();
	}

	// ---------------------------------------------------------------- ctors

	private Ctors ctors;

	/**
	 * Returns constructors collection.
	 * Creates new collection of first access.
	 */
	protected Ctors getCtors() {
		if (ctors == null) {
			ctors = new Ctors(this);
		}
		return ctors;
	}

	/**
	 * Returns the default ctor or <code>null</code> if not found.
	 */
	public CtorDescriptor getDefaultCtorDescriptor(boolean declared) {
		CtorDescriptor defaultCtor = getCtors().getDefaultCtor();

		if ((defaultCtor != null) && defaultCtor.matchDeclared(declared)) {
			return defaultCtor;
		}
		return null;
	}

	/**
	 * Returns the constructor identified by arguments or <code>null</code> if not found.
	 */
	public CtorDescriptor getCtorDescriptor(Class[] args, boolean declared) {
		CtorDescriptor ctorDescriptor = getCtors().getCtorDescriptor(args);

		if ((ctorDescriptor != null) && ctorDescriptor.matchDeclared(declared)) {
			return ctorDescriptor;
		}
		return null;
	}

	/**
	 * Returns an array of all {@link CtorDescriptor constructor descriptors}.
	 */
	public CtorDescriptor[] getAllCtorDescriptors() {
		return getCtors().getAllCtorDescriptors();
	}

}