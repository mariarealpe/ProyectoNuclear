# Sistema de Gestión de Prácticas Empresariales CUE

Plataforma web fullstack para gestionar prácticas empresariales de estudiantes de ingeniería en la Universidad Alexander Von Humboldt.

## Quick Start

### Requirements
- Java 17+
- Node.js 18+
- PostgreSQL o MySQL
- Maven 3.8+

### Backend Setup
```bash
cd PracticasEmpresariales/backend
mvn spring-boot:run -P postgres   # Con PostgreSQL (default)
# O si usas MySQL:
mvn spring-boot:run -P mysql
```

Backend available at: `http://localhost:8080`

### Frontend Setup
```bash
cd PracticasEmpresariales/frontend
npm install
npm run dev
```

Frontend available at: `http://localhost:5173`

## Project Structure

```
├── PracticasEmpresariales/
│   ├── backend/          Spring Boot backend
│   ├── frontend/         React frontend
│   └── docs/             Product documentation
├── docs/                 Functional requirements (Markdown)
└── .claude/              Claude Code configuration
```

## Git Workflow

1. **Always work in a new branch** (never commit to `main`)
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Commit with clear messages** (use `/commit` skill)
   ```bash
   git add .
   # Use `/commit` skill for proper formatting
   ```

3. **Push and create PR** when done
   ```bash
   git push -u origin feature/your-feature-name
   # Create PR on GitHub with detailed description
   ```

## Documentation

- **Functional Requirements**: See `docs/REQUISITOS_FUNCIONALES*.md`
- **Project Context**: See `.claude/CLAUDE.md`
- **Development Guide**: See this file

## Key Modules

| Module | Description |
|--------|-------------|
| MÓDULO 01 | Dashboard personalizado por rol |
| MÓDULO 02 | Gestión de usuarios y acceso |
| Vacantes | Gestión de vacantes empresariales |
| Asignaciones | Asignación de estudiantes a prácticas |
| Evaluaciones | Evaluaciones y calificaciones |

## Testing

### Backend
```bash
mvn clean test
```

### Frontend
```bash
npm run test
```

## Build

### Backend
```bash
mvn clean package
```

### Frontend
```bash
npm run build
```

## Tools & Skills Available

- `/commit` — Format commits with Sentry conventions
- `/pr-writer` — Write PR descriptions
- `/agents-md` — Update CLAUDE.md documentation
- `/find-bugs` — Pre-PR security/bug review

## Tech Stack

- **Backend**: Spring Boot 3.x, Spring Security, JWT, Hibernate/JPA
- **Frontend**: React 18+, TypeScript, Tailwind CSS, Vite
- **Database**: PostgreSQL / MySQL (configurable via Maven profiles)
- **Auth**: JWT tokens, role-based access control

## Contributing

1. Create a feature branch
2. Make your changes
3. Run tests
4. Commit with clear messages (use `/commit` skill)
5. Push and create PR with detailed description
6. Request review

## Deployment

Currently not deployed. Deployment platform TBD.

## Support

For questions about project setup or architecture, refer to `.claude/CLAUDE.md`
