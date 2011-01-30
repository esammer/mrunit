# Overview #

MRUnit is a unit test driver for MapReduce programs for use with JUnit. See the
overview in the Javadoc for more details.

This is a fork of the MRUnit code in Apache Hadoop's contrib directory and has
been created to quickly test and develop new features. Notable changes from
Apache Hadoop's contrib MRUnit:

* Build system converted to Maven from Ant / Ivy.
* Cleaned up to produce no warnings from javac.

Planned features:

* Port of MRUnit to the "new" API (mapreduce). This will be in addition to the
  old (mapred) API.
* Support for testing {Input,Output}Formats.
* Support for testing Writable serialization of custom types.

# Intentions #

Some members of the Apache Hadoop community feel like the contrib directory is
not a good long term home for projects like MRUnit. I agree. I would hope (and
intend to file a JIRA at some point) to deprecate and remove MRUnit from the
Hadoop contrib tree in favor of a standalone version hosted on Github as a
separate project. My belief is that MRUnit will benefit from a more frequent
release cycle and an independent schedule. The code will remain under the
Apache License.

Should the Apache Hadoop community elect to retain MRUnit in the contrib tree,
I intend to attempt to contribute changes upstream to the ASF.

# Authors #

Aaron Kimball <akimball83@gmail.com>
E. Sammer <esammer@cloudera.com>
