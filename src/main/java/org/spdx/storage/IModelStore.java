/**
 * Copyright (c) 2019 Source Auditor Inc.
 *
 * SPDX-License-Identifier: Apache-2.0
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.spdx.storage;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.spdx.core.InvalidSPDXAnalysisException;
import org.spdx.core.TypedValue;

/**
 * Service Provider Interface for storing and retrieving SPDX properties from the underlying store.
 * 
 * The interface uses the URI to identify specific objects stored.
 * 
 * Each object can have property values and property value lists associated with them.  
 * 
 * A property value is an object of a primitive type (e.g. String or Boolean) or is another
 * object which includes it's own ID and must also have a type described in the SPDX model.
 * 
 * A property list is just a list of values.
 * 
 * @author Gary O'Neall
 *
 */
public interface IModelStore extends AutoCloseable {
	
	public interface IModelStoreLock {
		public void unlock();
	}
	
	@FunctionalInterface
	public static interface ModelUpdate {
		void apply() throws InvalidSPDXAnalysisException;
	}
	
	/**
	 * Different types of ID's
	 */
	public enum IdType {
		LicenseRef, 		// ID's that start with LicenseRef-
		DocumentRef, 		// ID's that start with DocumentRef- - for compatibility in SPDX 2.X versions
		SpdxId, 			// ID's that start with SpdxRef-
		ListedLicense, 		// ID's associated with listed licenses
		Anonymous, 			// ID's for object only referenced internally
		Unkown}				// ID's that just don't fit any pattern

	/**
	 * @param objectUri unique URI within the SPDX model store for the objects
	 * @return true if the objectUri already exists for the document
	 */
	public boolean exists(String objectUri);

	/**
	 * Create a new object with objectUri, type and version from the typedValue
	 * @param typedValue TypedValue of the item to create
	 * @throws InvalidSPDXAnalysisException on model store errors 
	 */
	public void create(TypedValue typedValue) throws InvalidSPDXAnalysisException;

	/**
	 * @param objectUri unique URI within the SPDX model store for the objects
	 * @return Property descriptors for all properties having a value for a given objectUri within a document
	 * @throws InvalidSPDXAnalysisException on model store errors 
	 */
	public List<PropertyDescriptor> getPropertyValueDescriptors(String objectUri) throws InvalidSPDXAnalysisException;

	/**
	 * Sets a property value for a String or Boolean type of value creating the propertyDescriptor if it does not exist
	 * @param objectUri unique URI within the SPDX model store for the objects
	 * @param propertyDescriptor descriptor for the property
	 * @param value value to set
	 * @throws InvalidSPDXAnalysisException on model store errors 
	 */
	public void setValue(String objectUri, PropertyDescriptor propertyDescriptor, Object value) throws InvalidSPDXAnalysisException;

	/**
	 * @param objectUri unique URI within the SPDX model store for the objects
	 * @param propertyDescriptor descriptor for the property
	 * @return the single value associated with the objectUri, propertyDescriptor and document
	 * @throws InvalidSPDXAnalysisException on model store errors 
	 */
	public Optional<Object> getValue(String objectUri, PropertyDescriptor propertyDescriptor) throws InvalidSPDXAnalysisException;

	/**
	 * Generate a unique ID within the model store - Note: for a full URI, the id should be prepended with a URI prefix
	 * @param idType Type of ID
	 * @return next available unique ID for the specific idType - Note: for a full URI, the id should be prepended with a URI prefix
	 * @throws InvalidSPDXAnalysisException on model store errors 
	 */
	public String getNextId(IdType idType) throws InvalidSPDXAnalysisException;
	
	/**
	 * Removes a property from the document for the given ID if the property exists.  Does not raise any exception if the propertyDescriptor does not exist
	 * @param objectUri unique URI within the SPDX model store for the objects
	 * @param propertyDescriptor descriptor for the property
	 * @throws InvalidSPDXAnalysisException on model store errors
	 */
	public void removeProperty(String objectUri, PropertyDescriptor propertyDescriptor) throws InvalidSPDXAnalysisException;

	/**
	 * @param nameSpace Optional SPDX namespace to filter items by
	 * @param typeFilter Optional parameter to specify the type of objects to be retrieved
	 * @return Stream of all items store within the document
	 * @throws InvalidSPDXAnalysisException on model store errors
	 */
	public Stream<TypedValue> getAllItems(@Nullable String nameSpace, @Nullable String typeFilter) throws InvalidSPDXAnalysisException;
	
	/**
	 * Enter a critical section. leaveCriticialSection must be called.
	 * @param readLockRequested true implies a read lock, false implies write lock.
	 * @throws InvalidSPDXAnalysisException on model store errors 
	 */
	public IModelStoreLock enterCriticalSection(boolean readLockRequested) throws InvalidSPDXAnalysisException;
	
	/**
	 * Leave a critical section. Releases the lock form the matching enterCriticalSection
	 */
	public void leaveCriticalSection(IModelStoreLock lock);

	/**
	 * Removes a value from a collection of values associated with a property
	 * @param objectUri unique URI within the SPDX model store for the objects
	 * @param propertyDescriptor descriptor for the property
	 * @param value Value to be removed
	 * @return true if the value was removed
	 */
	public boolean removeValueFromCollection(String objectUri, PropertyDescriptor propertyDescriptor, Object value) throws InvalidSPDXAnalysisException;

	/**
	 * @param objectUri unique URI within the SPDX model store for the objects
	 * @param propertyDescriptor descriptor for the property
	 * @return size of a collection associated with a property.  0 if the property does not exist.
	 */
	public int collectionSize(String objectUri, PropertyDescriptor propertyDescriptor) throws InvalidSPDXAnalysisException;

	/**
	 * @param objectUri unique URI within the SPDX model store for the objects
	 * @param propertyDescriptor descriptor for the property
	 * @param value value to check for contains
	 * @return true if the collection associated with a property contains the value
	 */
	public boolean collectionContains(String objectUri, PropertyDescriptor propertyDescriptor, Object value) throws InvalidSPDXAnalysisException;
	
	/**
	 * Sets the value collection for the property to an empty collection creating the propertyDescriptor if it does not exist
	 * @param objectUri unique URI within the SPDX model store for the objects
	 * @param propertyDescriptor descriptor for the property
	 * @throws InvalidSPDXAnalysisException on model store errors 
	 */
	void clearValueCollection(String objectUri, PropertyDescriptor propertyDescriptor) throws InvalidSPDXAnalysisException;

	/**
	 * Adds a value to a property collection creating the propertyDescriptor if it does not exist
	 * @param objectUri unique URI within the SPDX model store for the objects
	 * @param propertyDescriptor descriptor for the property
	 * @param value value to add
	 * @return true if the collection was modified
	 * @throws InvalidSPDXAnalysisException on model store errors 
	 */
	public boolean addValueToCollection(String objectUri, PropertyDescriptor propertyDescriptor, Object value) throws InvalidSPDXAnalysisException;

	/**
	 * @param objectUri unique URI within the SPDX model store for the objects
	 * @param propertyDescriptor descriptor for the property
	 * @return Iterator over the list of values associated with the objectUri, propertyDescriptor and document
	 * @throws InvalidSPDXAnalysisException on model store errors 
	 */
	public Iterator<Object> listValues(String objectUri, PropertyDescriptor propertyDescriptor) throws InvalidSPDXAnalysisException;

	/**
	 * @param objectUri unique URI within the SPDX model store for the objects
	 * @param propertyDescriptor descriptor for the property
	 * @param clazz Class to test compatibility with
	 * @return true if all members of a collection associated with the objectUri and propertyDescriptor can be assigned to the clazz
	 * @throws InvalidSPDXAnalysisException on model store errors
	 */
	public boolean isCollectionMembersAssignableTo(String objectUri, PropertyDescriptor propertyDescriptor, Class<?> clazz) throws InvalidSPDXAnalysisException;
	
	/**
	 * @param objectUri unique URI within the SPDX model store for the objects
	 * @param propertyDescriptor descriptor for the property
	 * @param clazz Class to test compatibility with
	 * @param specVersion version of the SPDX to check against
	 * @return true if the value associated with the objectUri and propertyDescriptor can be assigned to the clazz
	 * @throws InvalidSPDXAnalysisException on model store errors
	 */
	public boolean isPropertyValueAssignableTo(String objectUri, PropertyDescriptor propertyDescriptor, 
			Class<?> clazz, String specVersion) throws InvalidSPDXAnalysisException;

	/**
	 * @param objectUri unique URI within the SPDX model store for the objects
	 * @param propertyDescriptor descriptor for the property
	 * @return true if the propertyDescriptor represents multiple values
	 */
	public boolean isCollectionProperty(String objectUri, PropertyDescriptor propertyDescriptor) throws InvalidSPDXAnalysisException;

	/**
	 * @param objectUri URI for the object or the anon. ID
	 * @return The type of ID based on the string format
	 */
	public IdType getIdType(String objectUri);

	/**
	 * In SPDX 2.2 license refs are allowed to be matched case insensitive.  This function will return
	 * the case sensitivie ID (e.g. if you have LicenseRef-ABC, calling this function with licenseref-abc will return LicenseRef-ABC
	 * @param nameSpace the nameSpace used for the ID - the URI is formed by the nameSpace + "#" + caseInsensisitiveId
	 * @param caseInsensisitiveId ID - case will be ignored
	 * @return the case sensitive ID if it exists
	 */
	public Optional<String> getCaseSensisitiveId(String nameSpace, String caseInsensisitiveId);

	/**
	 * @param objectUri unique URI within the SPDX model store for the objects
	 * @return type TypedValue containing the type of the ModelObject related to the ID
	 */
	public Optional<TypedValue> getTypedValue(String objectUri) throws InvalidSPDXAnalysisException;
	/**
	 * Deletes an item from the document
	 * @param objectUri unique URI within the SPDX model store for the objects
	 * @throws InvalidSPDXAnalysisException on model store errors
	 */
	public void delete(String objectUri) throws InvalidSPDXAnalysisException;

	/**
	 * @param objectUri ID or URI for an item
	 * @return true if the ID is anonymous
	 */
	boolean isAnon(String objectUri);
}
