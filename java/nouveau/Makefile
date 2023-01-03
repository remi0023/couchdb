test:
	@cd server && mvn test

run: test
	@cd server && mvn exec:exec -Dexec.executable="java"

install-shaded-libs:
	@cd shaded-lucene-4.6.1 && mvn install
	@cd shaded-lucene-9.x && mvn install





