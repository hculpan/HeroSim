CP=$CLASSPATH
CP=$CP:./target/scriptbuilder/lib/MRJAdapter.jar
CP=$CP:./target/scriptbuilder/lib/jdom-1.0.jar
CP=$CP:./target/scriptbuilder/lib/commons-cli-1.0.jar
CP=$CP:./target/scriptbuilder/lib/commons-lang-2.0.jar
CP=$CP:./target/scriptbuilder/lib/log4j-1.2.8.jar
CP=$CP:./target/herosim-0.5.jar

java -cp $CP org.culpan.herosim.inputchar.InputChar $*
