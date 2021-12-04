
JDK = "%JAVA_HOME%"

from glob import glob as folder_lookup
from shutil import make_archive

import os

DIR = os.getcwd().replace('\\', '/')
if DIR.endswith('/'): DIR = DIR[:len(DIR) - 1]

javac = '\"' * 1 + f"{JDK}/bin/javac" + '\"' * 1

java_files = folder_lookup(f"{DIR}/source/esrc/lang/*.java")

source_files = ""

for java_file in java_files:
    file_tokens = java_file.replace('\\', '/').split('/')
    file_name = file_tokens[len(file_tokens) - 1]
    source_files += file_name + ' '

os.chdir(f"{DIR}/source/esrc/lang")
os.system(f"\"{javac}\" -Xlint:deprecation -nowarn -source 8 -target 8 {source_files}")
os.chdir(DIR)

build_files = folder_lookup(f"{DIR}/source/esrc/lang/*")

if not os.path.exists(f"{DIR}/compiled/classpath/esrc/lang"):
    os.makedirs(f"{DIR}/compiled/classpath/esrc/lang")

if not os.path.exists(f"{DIR}/compiled/classpath/META-INF"):
    os.mkdir(f"{DIR}/compiled/classpath/META-INF")
    manifest = open(f"{DIR}/compiled/classpath/META-INF/MANIFEST.MF", 'w')
    manifest.write("Manifest-Version: 1.0\nMain-Class: esrc.lang.Core\n")
    manifest.close()

for build_file in build_files:
    if build_file.endswith(".java"): continue
    file_tokens = build_file.replace('\\', '/').split('/')
    file_name = file_tokens[len(file_tokens) - 1]
    destination = f"{DIR}/compiled/classpath/esrc/lang/{file_name}"
    if os.path.exists(destination): os.remove(destination)
    if build_file.endswith(".class"): os.rename(build_file, destination)
    else:
        dest_file = open(destination, "wb")
        cur_file = open(build_file, "rb")
        dest_file.write(cur_file.read())
        dest_file.close()
        cur_file.close()

make_archive(f"{DIR}/compiled/ESRCCore", "zip", f"{DIR}/compiled/classpath")
if os.path.exists(f"{DIR}/compiled/ESRCCore.jar"): os.remove(f"{DIR}/compiled/ESRCCore.jar")
os.rename(f"{DIR}/compiled/ESRCCore.zip", f"{DIR}/compiled/ESRCCore.jar")
