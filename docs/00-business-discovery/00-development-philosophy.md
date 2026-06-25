# Development Philosophy

## Document Information

| Field    | Value                                                        |
| -------- | ------------------------------------------------------------ |
| Project  | InfraTrack                                                   |
| Document | Development Philosophy                                       |
| Version  | 1.0                                                          |
| Status   | Living Document                                              |
| Audience | Product Owners, Business Analysts, Architects and Developers |

---

# Purpose

This document defines the principles that guide every decision made throughout the design and development of InfraTrack.

Rather than describing implementation details, this document explains how decisions are made, how business requirements are interpreted and how the product evolves over time.

These principles are intended to preserve consistency throughout the project's lifetime.

---

# 1. Product Before Portfolio

InfraTrack is developed as a realistic enterprise software product.

Although it is an open-source project, every design decision should assume that the software is intended for deployment within a real Australian Local Government.

The project should never contain portfolio-specific language such as:

* Demo Project
* Learning Project
* Portfolio Application
* Sample Software

InfraTrack should always present itself as a production-oriented software product.

---

# 2. Business Before Technology

Business processes define the software.

Technology never defines the business.

Whenever a new feature is proposed, the first question should always be:

> How does a real council perform this process today?

Only after understanding the operational reality should a software solution be designed.

Implementation choices are always secondary to business requirements.

---

# 3. Reality Before Assumptions

Business rules should never be invented simply because they appear logical.

Whenever possible, every significant business decision should be supported by:

* Australian Local Government practices;
* official publications;
* operational procedures;
* recognised industry standards;
* publicly available documentation.

If no reliable reference exists, the decision must be clearly identified as an InfraTrack design decision rather than an industry fact.

---

# 4. Separate Business Reality from Software Design

InfraTrack distinguishes between:

## Business Reality

How Australian Local Governments typically operate.

and

## InfraTrack Design

How InfraTrack chooses to model that behaviour.

This distinction prevents software implementation decisions from being presented as industry practices.

---

# 5. Evidence Matters

InfraTrack is not simply a maintenance application.

It is an operational evidence platform.

Every significant action should contribute to the historical record of an asset.

Examples include:

* inspections;
* photographs;
* maintenance reports;
* work orders;
* contractor reports;
* approvals;
* generated documents;
* operational decisions.

The complete history of an asset should remain available throughout its lifecycle.

---

# 6. Documentation is a First-Class Deliverable

Documentation is considered part of the product.

Business documentation, architectural documentation and operational documentation should evolve alongside the implementation.

Software should never become the only source of knowledge.

Whenever business behaviour changes, the corresponding documentation should be reviewed.

---

# 7. Simplicity Over Technical Complexity

InfraTrack intentionally favours explicit and understandable solutions.

Complex business domains should not automatically result in complex software architecture.

Whenever possible:

* keep workflows explicit;
* minimise abstraction;
* prefer readability over cleverness;
* optimise maintainability.

Complexity should exist only where it provides measurable business value.

---

# 8. Technology Should Remain Replaceable

Business rules should remain independent from technical implementation details.

The product should describe:

* assets;
* inspections;
* work orders;
* maintenance;
* validation.

Not:

* controllers;
* frameworks;
* databases;
* programming languages.

Technology choices may evolve without requiring business redesign.

---

# 9. Every Feature Must Solve a Real Problem

Features should never be added because they are technically interesting.

Before accepting a feature, the following question should be answered:

> Which operational problem does this solve for a council?

If no clear answer exists, the feature should not be implemented.

---

# 10. Every Decision Should Be Justifiable

Every important design decision should be explainable.

The preferred order of justification is:

1. Business requirement
2. Industry practice
3. User experience
4. Technical implementation

Technical preference alone should rarely justify a major decision.

---

# 11. Build for a Small Council

InfraTrack is intentionally designed for small to medium Australian Local Governments.

The objective is not to replace enterprise ERP systems.

The software should remain:

* understandable;
* affordable;
* maintainable;
* deployable by small IT teams.

Scope discipline is essential.

---

# 12. Design for Evolution

Infrastructure management evolves continuously.

The platform should therefore evolve through incremental improvements rather than disruptive redesigns.

Backward compatibility should be preserved whenever practical.

Future requirements should be anticipated without introducing unnecessary complexity.

---

# 13. Consistency Above Everything

Consistency is considered more valuable than novelty.

User experience, terminology, workflows, APIs and documentation should all describe the same business concepts using the same vocabulary.

Every term should have one official meaning throughout the project.

---

# 14. English is the Project Language

English is the official language of the project.

The following should always be written in English:

* documentation;
* source code;
* comments;
* commit messages;
* pull requests;
* API documentation;
* generated reports;
* user interfaces;
* sample data.

Maintaining a single project language improves consistency and reflects the target audience.

---

# 15. Professionalism Over Perfection

InfraTrack does not aim to demonstrate every possible technology.

Its objective is to demonstrate disciplined software engineering.

Success is measured by:

* clarity;
* consistency;
* maintainability;
* traceability;
* business accuracy;
* quality of documentation;
* architectural coherence.

A smaller, well-designed feature set is preferable to a larger but inconsistent system.

---

# 16. Learn Before Building

Before implementing a business process, the team should first understand how that process operates in the real world. Research, observation and business analysis take precedence over implementation. Software should be the result of understanding the domain, not the starting point of that understanding.

---

# Final Principle

InfraTrack exists to model the operational reality of Australian Local Governments as faithfully as possible.

Every document, workflow, business rule and software component should contribute towards that single objective.

Whenever uncertainty arises, the guiding question should always be:

> **"Would this make sense to a real council?"**

If the answer is no, the design should be reconsidered before implementation.
