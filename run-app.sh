#!/usr/bin/env bash

# Colores para la salida en consola
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # Sin color
BLUE='\033[0;34m'
YELLOW='\033[1;33m'

echo -e "${BLUE}===================================================${NC}"
echo -e "${BLUE}        Iniciando script de ejecución de App        ${NC}"
echo -e "${BLUE}===================================================${NC}"

# 1. Resolver el SDK de Android
LOCAL_SDK_DIR=""
if [ -f local.properties ]; then
    LOCAL_SDK_DIR=$(grep -E "^sdk.dir=" local.properties | cut -d'=' -f2)
    # Limpiar comillas y espacios en blanco
    LOCAL_SDK_DIR=$(echo "$LOCAL_SDK_DIR" | tr -d '"' | tr -d "'" | xargs)
fi

if [ -n "$ANDROID_HOME" ]; then
    SDK_PATH="$ANDROID_HOME"
elif [ -n "$ANDROID_SDK_ROOT" ]; then
    SDK_PATH="$ANDROID_SDK_ROOT"
elif [ -n "$LOCAL_SDK_DIR" ]; then
    SDK_PATH="$LOCAL_SDK_DIR"
else
    SDK_PATH="$HOME/Android/Sdk"
fi

echo -e "${YELLOW}🔍 Android SDK ubicado en:${NC} $SDK_PATH"

# 2. Localizar binarios de emulator y adb
EMULATOR_BIN="$SDK_PATH/emulator/emulator"
ADB_BIN="$SDK_PATH/platform-tools/adb"

# Fallback si no están en la ruta del SDK específica
if [ ! -f "$EMULATOR_BIN" ]; then
    if command -v emulator >/dev/null 2>&1; then
        EMULATOR_BIN="emulator"
    else
        echo -e "${RED}❌ Error: No se encontró el binario 'emulator'.${NC}"
        echo -e "Asegúrate de que la variable ANDROID_HOME esté definida o que 'sdk.dir' en local.properties sea correcta."
        exit 1
    fi
fi

if [ ! -f "$ADB_BIN" ]; then
    if command -v adb >/dev/null 2>&1; then
        ADB_BIN="adb"
    else
        echo -e "${RED}❌ Error: No se encontró el binario 'adb'.${NC}"
        echo -e "Asegúrate de que la variable ANDROID_HOME esté definida o que 'sdk.dir' en local.properties sea correcta."
        exit 1
    fi
fi

# 3. Determinar el dispositivo/AVD a usar
# Prioridad: 1. Argumento de línea de comando ($1)
#            2. Variable de entorno EMULATOR_AVD
#            3. Default (Medium_Phone_API_36)
DEFAULT_AVD="Medium_Phone_API_36"
AVD_NAME="${EMULATOR_AVD:-$DEFAULT_AVD}"

if [ -n "$1" ]; then
    AVD_NAME="$1"
fi

echo -e "${YELLOW}📱 Usando AVD (Emulador):${NC} $AVD_NAME"

# 4. Verificar si ya hay dispositivos o emuladores en ejecución
DEVICES_LIST=$($ADB_BIN devices | grep -v "List of devices attached" | grep "device" | awk '{print $1}')
CONNECTED_DEVICES_COUNT=$(echo "$DEVICES_LIST" | grep -v '^$' | wc -l)

if [ "$CONNECTED_DEVICES_COUNT" -eq 0 ]; then
    echo -e "${YELLOW}⏳ No se detectaron dispositivos encendidos. Iniciando emulador '$AVD_NAME'...${NC}"
    
    # Verificar si el AVD especificado existe
    AVDS_DISPONIBLES=$($EMULATOR_BIN -list-avds)
    AVD_EXISTS=$(echo "$AVDS_DISPONIBLES" | grep -x "$AVD_NAME" | wc -l)
    
    if [ "$AVD_EXISTS" -eq 0 ]; then
        echo -e "${RED}⚠️  El emulador '$AVD_NAME' no fue encontrado en la lista de AVDs creados.${NC}"
        echo -e "Emuladores disponibles:"
        echo -e "$AVDS_DISPONIBLES"
        echo -e ""
        
        # Si hay algún emulador disponible, sugerir el primero o preguntar
        PRIMER_AVD=$(echo "$AVDS_DISPONIBLES" | head -n 1)
        if [ -n "$PRIMER_AVD" ]; then
            if [ -t 0 ]; then
                echo -n -e "${YELLOW}¿Deseas iniciar '$PRIMER_AVD' en su lugar? [S/n]: ${NC}"
                read -r respuesta
                respuesta=${respuesta:-S}
            else
                respuesta="S"
                echo -e "${YELLOW}Usando automáticamente '$PRIMER_AVD' ya que no hay una terminal interactiva.${NC}"
            fi
            
            if [[ "$respuesta" =~ ^[Ss]$ ]]; then
                AVD_NAME="$PRIMER_AVD"
            else
                echo -e "${RED}❌ Operación cancelada. Crea el emulador '$AVD_NAME' en Android Studio o pásalo como argumento.${NC}"
                exit 1
            fi
        else
            echo -e "${RED}❌ No se encontraron emuladores creados. Por favor crea uno en Android Studio primero.${NC}"
            exit 1
        fi
    fi
    
    # Iniciar el emulador en segundo plano
    $EMULATOR_BIN -avd "$AVD_NAME" > /dev/null 2>&1 &
    EMULATOR_PID=$!
    echo -e "${GREEN}🚀 Emulador lanzado en segundo plano (PID: $EMULATOR_PID). Esperando a que inicie...${NC}"
    
    # Esperar conexión básica de ADB
    $ADB_BIN wait-for-device
    
    # Esperar a que el sistema operativo de Android termine de bootear
    echo -e "${YELLOW}⏳ Esperando a que el sistema Android termine de bootear completamente...${NC}"
    while [ "$($ADB_BIN shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
        sleep 2
    done
    echo -e "${GREEN}✅ ¡Emulador iniciado y listo!${NC}"
else
    echo -e "${GREEN}✅ Dispositivo(s) detectado(s) en ejecución:${NC}"
    echo "$DEVICES_LIST"
fi

# 5. Compilar e instalar la aplicación
echo -e "\n${YELLOW}🏗️  Compilando e instalando la aplicación...${NC}"
./gradlew installDebug
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Error al compilar o instalar la aplicación.${NC}"
    exit 1
fi

# 6. Lanzar la aplicación en el emulador/dispositivo
PACKAGE_NAME="ar.edu.unlam.mobile.scaffolding"
MAIN_ACTIVITY=".MainActivity"

echo -e "\n${GREEN}🚀 Lanzando la aplicación ($PACKAGE_NAME/$MAIN_ACTIVITY)...${NC}"
$ADB_BIN shell am start -n "$PACKAGE_NAME/$MAIN_ACTIVITY"
if [ $? -eq 0 ]; then
    echo -e "${GREEN}===================================================${NC}"
    echo -e "${GREEN}   🎉 ¡Aplicación instalada y ejecutada con éxito!   ${NC}"
    echo -e "${GREEN}===================================================${NC}"
else
    echo -e "${RED}❌ Error al intentar abrir la aplicación.${NC}"
    exit 1
fi
