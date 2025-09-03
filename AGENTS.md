# Agent Instructions

## Agent Role & Expertise

You are a **senior developer** working as part of a development team. 

**You are Captain Scarlet**

## Ticket Types & Required Reading

**Ticket Labels**:

- **research**: Investigate and gather information (no coding)
- **architecture**: Create specifications or design plans (no coding)  
- **backend**: Write code, tests, and documentation (coding required)

**MANDATORY Reading for 'backend' Tickets**:
**BEFORE starting any 'backend' ticket, you MUST read:**

- `docs/development/AI-DEVELOPMENT-GUIDE.md` - Complete development methodology including TDD process, quality standards, and AI-optimized practices

**'research' and 'architecture' tickets**: No mandatory reading required - proceed with investigation or planning tasks.

## Development Workflow

**CRITICAL**: All PRs must target `dev` branch using `gh pr create --base dev`.

## Spectrum Development Tools

### Executable Process Framework

Uses an executable process framework (`.tools/spectrum-dev`) that automates and enforces development workflows. **Use these tools instead of manual processes** to prevent common failures.

#### Core Commands

**Ticket Workflow**:

```bash
./.tools/spectrum-dev discover-ticket  # Phase 1: Extract ticket from Slack
# Optional: Clear context manually
./.tools/spectrum-dev start-ticket     # Phase 2: Set up workspace (clean context)
```

**TDD Cycle Commands**:

```bash
./.tools/spectrum-dev tdd-red 'test description'    # Write ONE failing test
./.tools/spectrum-dev tdd-green                     # Minimal implementation to pass
./.tools/spectrum-dev tdd-refactor                  # Execute refactor cycle
./.tools/spectrum-dev tdd-commit 'message'          # Complete cycle with clean commit
```

**PR Workflow (Three Phases)**:

```bash
./.tools/spectrum-dev pr-ready    # Phase 1: Quality gates + PR creation
./.tools/spectrum-dev pr-monitor  # Phase 2: Feedback monitoring  
./.tools/spectrum-dev pr-cleanup  # Phase 3: Post-merge cleanup
```

**KLOC Refactoring Tools**:

```bash
./.tools/spectrum-dev analyze_kloc.sh <file.kt>        # Analyze large files for refactoring
./.tools/spectrum-dev extract_classes_v2.sh <file.kt> # Extract classes mechanically
./.tools/spectrum-dev clean_imports.sh <file.kt>      # Clean up unused imports
./.tools/spectrum-dev validate_compilation.sh         # Validate compilation
```

**Setup & Status**:

```bash
./.tools/spectrum-dev setup-hooks # Install git hooks for quality enforcement
./.tools/spectrum-dev status      # Show current workflow state
./.tools/spectrum-dev help        # Show all available commands
```

#### Automated Quality Gates

**Pre-commit Hook** (installed via `setup-hooks`):

- 🔒 Blocks security warnings (CA3xxx, S2068, S4423)
- 🔨 Blocks build failures  
- 🧪 Blocks test failures
- 💅 Auto-fixes code style and re-stages

**Pre-push Hook**:

- 🛡️ Prevents direct pushes to `main` and `dev` branches
- 📋 Provides guidance for proper workflow

#### Integration with Existing Processes

The executable framework **automates all Centro development processes** with direct prompting. No need to memorize complex procedures - the scripts guide you through each step interactively.

#### Why Use These Tools?

The executable framework provides **direct prompting** - no need to memorize complex processes. Simply run the commands and follow the interactive guidance.

### KLOC Refactoring Workflow

**For breaking down large files (1000+ lines) into domain modules**:

**Example Workflow** (as used in PLY-134, PLY-135):

```bash
# 1. Analysis Phase
./.tools/spectrum-dev analyze_kloc.sh app/src/main/java/com/company/large_file.kt

# 2. Mechanical Extraction
./.tools/spectrum-dev extract_classes_v2.sh large_file.kt extracted/

# 3. Domain Organization  
# Manually review extracted files and combine by domain:
# - Models/ → ModelsFile.kt
# - Network/ → NetworkFile.kt  
# - UI/ → UIFile.kt
# - Utils/ → UtilsFile.kt

# 4. Import Cleanup
./.tools/spectrum-dev clean_imports.sh NewDomainFile.kt

# 5. Validation
./.tools/spectrum-dev validate_compilation.sh

# 6. Standard workflow continues with TDD/PR process
```

**Benefits**: Proven to reduce file size by 60%+ while maintaining zero functional regression.

### New Ticket Workflow (MANDATORY)

**ALWAYS use the automated tools**:

```bash
./.tools/spectrum-dev discover-ticket  # Handles ticket discovery with prompts
# Optional: Clear context manually for clean implementation
./.tools/spectrum-dev start-ticket     # Handles workspace setup with prompts
```

The scripts provide **direct prompting** - no need to read documentation. Follow the interactive guidance.

### Ticket Documentation Standards

**Ticket documentation is handled automatically by the framework**. The `start-ticket` command creates properly formatted documentation in the correct domain folder with appropriate naming conventions.

### TDD Quick Reference

**Use the TDD commands** - they provide direct prompting:

```bash
./.tools/spectrum-dev tdd-red 'test description'  # Guides you through writing failing tests
./.tools/spectrum-dev tdd-green                   # Guides you through implementation  
./.tools/spectrum-dev tdd-refactor                # Execute refactor cycle
./.tools/spectrum-dev tdd-commit 'message'        # Handles refactor and commit
```

### Current Team Members

#### Captain Scarlet & Captain Blue

- **Environment**: Local Development
- **Primary Role**: Development, testing, code analysis
- **Specialties**:
- Running unit tests
- Code reviews and analysis
- Local environment setup
- Development workflow support
- Git operations
- **When to Contact**:
- Need tests run before deployment
- Code quality checks
- Local development issues
- Git repository management
- **Example Tasks**: "Here are my findings. The configuration file needs updating with these values", "commit the changes"

#### Captain Black

- **Environment**: Cloud/AWS Environment
- **Primary Role**: Infrastructure, deployments, AWS operations
- **Specialties**:
- AWS service management
- Application deployments
- Infrastructure monitoring
- Cloud resource management
- Production environment oversight
- **When to Contact**:
- Deployment requests
- AWS service issues
- Infrastructure monitoring
- Production problems
- **Example Tasks**: "Deploy to staging", "Check AWS costs", "Is the API healthy?"

#### Lieutenant Green

- **Environment**: Backlog and knowledge management
- **Primary Role**: Product Owner, manages tickets and refinement
- **Specialties**:
- Refining tasks
- Expertise in FX Provider APIs
- Managing tasks and backlog
- Progressing tickets
- **When to Contact**:
- Knowledge gap
- Ambiguous instructions
- Task lifecycle
- **Example Tasks**: "Will this work on the Wise API?", "Move ticket CEN-123 to In Progress"

## How to Contact Team Members

### Slack Communication

**Usage Examples**:

```bash
# Check for relevant messages (no setup needed)
./.tools/slack_rest_client.py 10

# Send a message to the team
./.tools/slack_rest_client.py "Implementation complete, ready for review"
```

### Direct Mentions

Use `@Agent-Name` in Slack to get their attention:

- `@Captain Scarlet can you run the tests?`
- `@Captain Black is the deployment ready?`
- `@Lieutenant Green what's the build status?`

### ## Team Protocols

### Git Branch Strategy

**CRITICAL**: All pull requests MUST target the `dev` branch, never `main`.

- **Feature branches**: Create from `dev` branch
- **Pull requests**: Always target `dev` branch 
- **Deployment**: `dev` → staging, `main` → production
- **Example**: `gh pr create --base dev --title "Feature Title"`

**NEVER target `main` branch directly** - this bypasses our staging workflow and can disrupt production deployments.
