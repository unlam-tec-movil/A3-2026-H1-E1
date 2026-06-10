---
name: design-driven-testing
description: Guidelines and workflow for designing and planning testing suites, particularly property-based testing. Always trigger this skill when the user asks to implement tests, design a test suite, write unit or integration tests, increase test coverage, or plan test infrastructure. It enforces a workflow: first, deliver a detailed Design Document; second, deliver a structured Phase-by-Phase Implementation Plan with checkboxes and dependency waves; and third, execute the entire test suite and verify correctness.
---

# Design-Driven Testing Workflow

This skill defines a workflow for implementing, designing, or enhancing testing suites (including unit tests, integration tests, and property-based tests). The core philosophy is that before writing any testing code, the agent must align on a rigorous design, produce a checklist-based implementation plan, and finally execute and validate the complete test suite.

Always perform these steps in order:

1. **Design Document**: Formal specification of test architecture, algorithms, and properties.
2. **Implementation Plan**: A phase-by-phase task list with checkboxes and dependency mappings.
3. **Execution and Validation**: Run all tests in the suite to verify compilation, style guidelines, and functional correctness.

---

## Step 1: The Design Document

The Design Document must be structured and detailed. It should contain the following sections:

### 1. Overview

Summary of the test suite enhancement, goals, target coverage, and core testing paradigms (e.g., Property-Based Testing alongside example-based testing).

### 2. Main Algorithm / Workflow

A Mermaid diagram (such as a sequence diagram) showing the interaction between the test suite, generators, validators, domain models, and storage/network layers.

### 3. Core Interfaces / Types

Kotlin code defining the interfaces and data classes for the test framework (e.g., generators, specifications, runners, configurations).

### 4. Key Functions with Formal Specifications

Formal preconditions, postconditions, loop invariants, and roundtrip invariants for key system functions. Use standard mathematical/logical notation when specifying invariants.

### 5. Algorithmic Pseudocode

Pascal-style pseudocode describing main verification algorithms, data generators, and shrinking logic.

### 6. Example Usage

Kotlin code snippets demonstrating how the tests (e.g., property-based tests) integrate with the existing test runner and assertions.

### 7. Correctness Properties

Universal invariants, idempotence, commutativity, and associativity specifications represented using formal logic notation (e.g., $\forall$, $\exists$, $\implies$).

### 8. Test Infrastructure Changes

Specific configuration updates, directory trees showing new test file placements, and the generated documentation output structure.

### 9. Data Generators Implementation Patterns

Concrete Kotlin examples of custom test data generators with constraints and shrinking support.

### 10. CI/CD Integration

A GitHub Actions workflow configuration (YAML) demonstrating how the tests and reporting are run automatically in a CI pipeline.

### 11. Assertion Patterns

Custom Kotlin assertion helpers designed specifically to verify coordinate precision, roundtrip consistency, or collections.

### 12. Test Execution and Reporting

Classes for gathering and writing Markdown/JSON statistics and reports from the runs.

### 13. Dependencies and Version Management

`libs.versions.toml` library declarations and version definitions.

---

## Step 2: The Implementation Plan

Once the design is agreed upon, generate a highly structured Implementation Plan (Tasks list) that contains:

### 1. Overview

Brief explanation of what the plan covers and any scope constraints (e.g., MVP vs Full Release).

### 2. Phase-by-Phase Tasks

Organize tasks chronologically into distinct Phases (e.g., Phase 1: Setup, Phase 2: Generators, etc.):

- Use checklist checkboxes `- [ ]` for each task.
- Explicitly trace each task back to the originating requirement or design section (e.g., `_Requirements: 2.1, 5.1_`).
- Place a **Checkpoint** at the end of each phase defining how to verify that phase was completed successfully.

### 3. Task Dependency Graph

A JSON block mapping tasks to "waves" based on execution order and dependencies:

```json
{
  "waves": [
    { "id": 0, "tasks": ["1", "2"] },
    { "id": 1, "tasks": ["3"] }
  ]
}
```

---

## Step 3: Execution and Validation

After executing the implementation plan tasks, the agent must validate the complete test suite:

1. **Format Check**: Run the ktlint style formatter `./gradlew :app:ktlintFormat` and checks `./gradlew :app:ktlintCheck` to ensure code style compliance.
2. **Compilation Verification**: Build the debug and test targets (e.g., `./gradlew assembleDebug` or `./gradlew testClasses`) to ensure zero compilation or linking errors.
3. **Run Tests**: Execute the unit and integration tests using `./gradlew test` (or specific target tests if applicable). Verify that:
   - All tests execute successfully.
   - The exit code of the test command is `0`.
   - The test counts are correct and no regressions are introduced.

---

## Example Formats

Refer to `.agents/design.md` for the exact visual style, layout, code style, and mathematical rigor expected of the Design Document, and `.agents/tasks.md` for the format and structure of the Implementation Plan.
