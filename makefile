SOURCE_DIR = ./src/
AGENTS_DIR = ./src/agents/
COMMON_DIR = ./src/common/

JADE_LIB = ./lib/jade.jar

JAVA_PATH = java
JAVAC_PATH = javac

JAVA_SOURCES = $(AGENTS_DIR)MainAgent.java $(AGENTS_DIR)Pavlov.java $(AGENTS_DIR)Pavlov_PSI_11.java $(AGENTS_DIR)RandomAgent.java $(AGENTS_DIR)SimpleAgent_PSI_11.java $(AGENTS_DIR)SpitefulAgent.java $(AGENTS_DIR)TftAgent.java $(AGENTS_DIR)PSI_11.java $(COMMON_DIR)GUI.java $(COMMON_DIR)GameParametersStruct.java $(COMMON_DIR)Stats.java

CLASS_FILES = $(JAVA_SOURCES:%.java=%.class)

%.class : %.java
	$(JAVAC_PATH) -classpath "$(JADE_LIB):$(SOURCE_DIR)" $<

all: $(CLASS_FILES)

clean:
	$(RM) $(AGENTS_DIR)*.class $(COMMON_DIR)*.class

#
# Convenient way to run the java programs
#

play: $(CLASS_FILES)
	$(JAVA_PATH) -classpath "$(JADE_LIB):$(SOURCE_DIR)" jade.Boot -name IPD_Tournament -agents "main:agents.MainAgent;random:agents.RandomAgent;tft:agents.TftAgent;PSI_11:agents.PSI_11;spite:agents.SpitefulAgent;pavlov_P11:agents.Pavlov_PSI_11;simple:agents.SimpleAgent_PSI_11;pavlov:agents.Pavlov"
