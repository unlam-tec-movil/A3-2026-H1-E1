#!/usr/bin/env bash

# Colores para la salida en consola
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # Sin color
BLUE='\033[0;34m'
YELLOW='\033[1;33m'

echo -e "${BLUE}===================================================${NC}"
echo -e "${BLUE}   Ejecutando verificaciones locales para el PR   ${NC}"
echo -e "${BLUE}===================================================${NC}"

# 1. Formatear código
echo -e "\n${YELLOW}🎨 [1/4] Formateando código con ktlint...${NC}"
./gradlew ktlintFormat
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Error al formatear el código. Verifique los errores mostrados arriba.${NC}"
    exit 1
fi

# 2. Pruebas unitarias
echo -e "\n${YELLOW}🧪 [2/4] Ejecutando pruebas unitarias locales...${NC}"
./gradlew :app:test
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Las pruebas unitarias fallaron. Por favor, corríjalas antes de subir el PR.${NC}"
    exit 1
fi

# 3. Reporte de cobertura Kover
echo -e "\n${YELLOW}📊 [3/4] Generando reporte de cobertura XML...${NC}"
./gradlew :app:koverXmlReportRelease
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Error al generar el reporte de cobertura de Kover.${NC}"
    exit 1
fi

# 4. Android Lint
echo -e "\n${YELLOW}🔍 [4/4] Verificando advertencias con Android Lint...${NC}"
./gradlew :app:lint
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Se encontraron advertencias graves en Android Lint.${NC}"
    exit 1
fi

echo -e "\n${GREEN}===================================================${NC}"
echo -e "${GREEN}   ✅ ¡Todas las verificaciones pasaron con éxito!   ${NC}"
echo -e "${GREEN}===================================================${NC}"
