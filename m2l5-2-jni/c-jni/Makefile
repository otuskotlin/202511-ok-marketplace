JDK=/usr/lib/jvm/java-21-openjdk-amd64
INC=-I../build/jniIncludes -I/usr/include -I/usr/include/x86_64-linux-gnu -I$(JDK)/include -I$(JDK)/include/linux

CC = gcc
OUT_FILE_NAME = libc_jni.so

CFLAGS= -fPIC -O0 -g -Wall -c

OBJ_DIR=../build/c/obj

OUT_DIR=../build/c/lib

.PHONY: rebuild all clean

all: $(OUT_FILE_NAME)

#Compiling every *.cpp to *.o
$(OBJ_DIR)/%.o: %.c dirmake
	$(CC) -c $(INC) $(CFLAGS) -o $@  $<

$(OUT_FILE_NAME): $(patsubst %.c,$(OBJ_DIR)/%.o,$(wildcard *.c))
	$(CC) -shared -o $(OUT_DIR)/$(OUT_FILE_NAME) $^

dirmake:
	@mkdir -p $(OUT_DIR)
	@mkdir -p $(OBJ_DIR)

clean:
	rm -f $(OBJ_DIR)/*.o $(OUT_DIR)/$(OUT_FILE_NAME) Makefile.bak

rebuild: clean all
