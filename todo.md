Needs you (outside the repo):
1. Replace the GitHub secrets SONATYPE_USER and SONATYPE_PASSWORD with Central Portal user tokens. The old logins no longer work.
2. Verify the dev.mongocamp namespace on central.sonatype.com.
3. The PGP secrets stay as they are.
4. micrometer dependency: investigate the `PersistenceSuite` failure with `mongodb-driver` 3.3.6 and `micrometer-mongodb` 1.0.3 (`micrometer-mongodb` 1.0.3 was compiled against a different `mongodb-driver` API than 3.3.6.)