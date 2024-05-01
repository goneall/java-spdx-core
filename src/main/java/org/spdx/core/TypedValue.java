package org.spdx.core;


/**
 * Value which is a stored typed item
 */
public class TypedValue {
	
	String objectUri;
	String type;
	String specVersion;
	
	public TypedValue(String objectUri, String type, String specVersion) throws SpdxInvalidIdException, SpdxInvalidTypeException, ModelRegistryException {
		if (objectUri == null) {
			throw new SpdxInvalidIdException("Null value Id");
		}
		if (type == null) {
			throw new SpdxInvalidTypeException("Null type");
		}
		if (specVersion == null) {
			throw new SpdxInvalidTypeException("Null specVersion");
		}
		if (ModelRegistry.getModelRegistry().typeToClass(type, specVersion) == null) {
			throw new SpdxInvalidTypeException("Unknown type "+type+" for spec Version "+specVersion);
		}
		this.objectUri = objectUri;
		this.type = type;
		this.specVersion = specVersion;
	}

	/**
	 * @return the objectUri
	 */
	public String getObjectUri() {
		return objectUri;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the specVersion
	 */
	public String getSpecVersion() {
		return specVersion;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof TypedValue)) {
			return false;
		}
		TypedValue tv = (TypedValue)o;
		return tv.getObjectUri().equals(this.objectUri) && tv.getType().equals(this.type);
	}
	
	@Override
	public int hashCode() {
		return 181 ^ this.objectUri.hashCode() ^ this.type.hashCode();
	}
}