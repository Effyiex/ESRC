
import os

DIR = os.getcwd().replace('\\', '/')
if DIR.endswith('/'): DIR = DIR[:len(DIR) - 1]

def invert(byte):
    byte += 128
    if byte > 255: byte -= 256
    return byte

INJECTION = "Bytecode; /*{INJECTION}*/"

source_read = open(f"{DIR}/source/esrc/wrapper/WindowsWrapper.cs", 'r')
source_code = source_read.read()
source_read.close()

core_read = open(f"{DIR}/compiled/ESRCCore.jar", "rb")
bytes = core_read.read()
core_read.close()

formatted_bytes = "Bytecode = new byte[] {"
for byte in bytes: formatted_bytes += f"{str(invert(byte))},"
formatted_bytes = formatted_bytes[:len(formatted_bytes) - 1]
formatted_bytes += "};"

source_write = open(f"{DIR}/compiled/ESRCWrapper.cs", 'w')
source_write.write(source_code.replace(INJECTION, formatted_bytes))
source_write.close()
