javac -d build/javac --source 8 --target 8 src/main/java/juuxel/projector/*
jar --create --file projector.jar --main-class juuxel.projector.Projector -C build/javac .
