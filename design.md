# Map and Location Tests Design

## Overview
Create JVM-compatible unit tests for map and location-related functionality. Tests will run on the JVM without requiring Android instrumentation.

## Test Scope

### 1. Clinic Data Parsing
- **Responsibility**: Test parsing of clinics JSON from assets
- **Tests**:
  - Parse valid JSON structure with meta and clinics array
  - Extract clinics array from wrapper object
  - Map JSON objects to Clinic domain models
  - Handle invalid/malformed JSON gracefully

### 2. Clinic Database Operations
- **Responsibility**: Test database population and retrieval
- **Tests**:
  - Save clinics to database
  - Retrieve clinics from database
  - Check if database has clinics (count query)
  - Handle empty database state
  - Verify clinics are persisted correctly

### 3. GeoJSON Conversion
- **Responsibility**: Test conversion of clinic data to GeoJSON format for map display
- **Tests**:
  - Convert Clinic to GeoJSON Feature
  - Create FeatureCollection from clinic list
  - Verify coordinate order (lng, lat)
  - Validate GeoJSON structure
  - Handle empty clinic list

### 4. Location Observable
- **Responsibility**: Test location data stream
- **Tests**:
  - Location updates are emitted
  - Location contains valid lat/lng
  - Handle location permission scenarios

### 5. UI State Management
- **Responsibility**: Test ViewModel state transitions
- **Tests**:
  - Initial state (loading clinics, no permission)
  - Clinics load success
  - Clinics load error handling
  - Permission granted transition
  - Permission denied transition
  - All state fields are properly updated

## Test Structure
- Tests organized by feature/use case
- Use mocks for external dependencies (database, location service)
- All tests runnable on JVM without Android instrumentation
- Tests are independent and can run in any order

## Tools
- JUnit 5
- Mockito for mocking
- Kotest or standard assertions
- Kotlin test coroutines
