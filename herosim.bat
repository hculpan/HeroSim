@echo off

set CP=%CLASSPATH%
set CP=%CP%;./target/scriptbuilder/lib/MRJAdapter.jar
set CP=%CP%;./target/scriptbuilder/lib/jdom-1.0.jar
set CP=%CP%;./target/scriptbuilder/lib/commons-cli-1.0.jar
set CP=%CP%;./target/scriptbuilder/lib/commons-lang-2.0.jar
set CP=%CP%;./target/scriptbuilder/lib/log4j-1.2.8.jar
set CP=%CP%;./target/herosim-0.5.jar

java -cp %CP% org.culpan.herosim.HeroSimMain %*