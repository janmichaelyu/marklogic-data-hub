# Contributing to MarkLogic Data Hub

MarkLogic Data Hub welcomes new contributors. This document will guide you
through the process.

 - [Issues and Bugs](#found-an-issue)
 - [Feature Requests](#want-a-feature)
 - [Building from Source](#building-the-framework-from-source)
 - [Submission Guidelines](#submission-guidelines)
 
## Found an Issue?
If you find a bug in the source code or a mistake in the documentation, you can help us by submitting an issue to our [GitHub Issue Tracker][issue tracker]. Even better you can submit a Pull Request
with a fix for the issue you filed.

## Want a Feature?
You can request a new feature by submitting an issue to our [GitHub Issue Tracker][issue tracker].  If you
would like to implement a new feature then first create a new issue and discuss it with one of our
project maintainers.

## Building the Framework from Source
Looking to build the code from source? Look no further.

#### Prerequisites
You need these to get started

- Java 8 JDK
- Gradle (3.1 or greater)
- Node JS 6.5 or newer
- Typings `npm -g install typings`
- A decent IDE. IntelliJ is nice.

#### Building from the command line
**First, a warning.** _The DHF has a ton of tests and they take a very long time to run. Considering you might not want to invest 30 minutes to wait for tests these instructions will show you how to skip the tests._

To build the entire DHF (marklogic-data-hub.jar, quickstart.war, and ml-data-hub-plugin for gradle) simply run this command:

```bash
cd /path/to/data-hub-project/
gradle build -x test
```

#### Running the QuickStart UI from source
Make sure you have the prerequisites installed.

You will need to open two terminal windows.

**Terminal window 1** - This runs the webapp.
```bash
cd /path/to/data-hub-project
gradle bootrun
```

**Terminal window 2** - This runs the Quickstart UI
```
cd /path/to/data-hub-project/quick-start
npm install
npm start
```

Now open your browser to [http://localhost:4200](http://localhost:4200) to use the debug version of the Quickstart UI.

## Submission Guidelines

### Submitting an Issue
Before you submit your issue search the archive, maybe your question was already answered.

If your issue appears to be a bug, and hasn't been reported, open a new issue.
Help us to maximize the effort we can spend fixing issues and adding new
features, by not reporting duplicate issues. Please fill out the issue template so that your issue can be dealt with quickly.

### Submitting a Pull Request

#### Fork marklogic-data-hub

Fork the project [on GitHub](https://github.com/marklogic/marklogic-data-hub/fork) and clone
your copy.

```sh
$ git clone git@github.com:username/marklogic-data-hub.git
$ cd marklogic-data-hub
$ git remote add upstream git://github.com/marklogic/marklogic-data-hub.git
```

We ask that you open an issue in the [issue tracker][] and get agreement from
at least one of the project maintainers before you start coding.

Nothing is more frustrating than seeing your hard work go to waste because
your vision does not align with that of a project maintainer.

#### Create a branch for your changes

Okay, so you have decided to fix something. Create a feature branch
and start hacking. **Note** that we use git flow and thus our most recent changes live on the 1.0-develop branch.

```sh
$ git checkout -b my-feature-branch -t origin/1.0-develop
```

#### Formatting code

We use [.editorconfig][] to configure our editors for proper code formatting. If you don't
use a tool that supports editorconfig be sure to configure your editor to use the settings
equivalent to our .editorconfig file.

#### Commit your changes

Make sure git knows your name and email address:

```sh
$ git config --global user.name "J. Random User"
$ git config --global user.email "j.random.user@example.com"
```

Writing good commit logs is important. A commit log should describe what
changed and why. Follow these guidelines when writing one:

1. The first line should be 50 characters or less and contain a short
   description of the change including the Issue number prefixed by a hash (#).
2. Keep the second line blank.
3. Wrap all other lines at 72 columns.

A good commit log looks like this:

```
Fixing Issue #123: make the whatchamajigger work in MarkLogic 8

Body of commit message is a few lines of text, explaining things
in more detail, possibly giving some background about the issue
being fixed, etc etc.

The body of the commit message can be several paragraphs, and
please do proper word-wrap and keep columns shorter than about
72 characters or so. That way `git log` will show things
nicely even when it is indented.
```

The header line should be meaningful; it is what other people see when they
run `git shortlog` or `git log --oneline`.

#### Rebase your repo

Use `git rebase` (not `git merge`) to sync your work from time to time.

```sh
$ git fetch upstream
$ git rebase upstream/1.0-develop
```


#### Test your code

Make sure the JUnit tests pass.

```sh
$ gradle test
```

Make sure that all tests pass. Please, do not submit patches that fail.

#### Push your changes

```sh
$ git push origin my-feature-branch
```

#### Submit the pull request

Go to https://github.com/username/marklogic-data-hub and select your feature branch. Click
the 'Pull Request' button and fill out the form.

Pull requests are usually reviewed within a few days. If you get comments
that need to be to addressed, apply your changes in a separate commit and push that to your
feature branch. Post a comment in the pull request afterwards; GitHub does
not send out notifications when you add commits to existing pull requests.

That's it! Thank you for your contribution!


#### After your pull request is merged

After your pull request is merged, you can safely delete your branch and pull the changes
from the main (upstream) repository:

* Delete the remote branch on GitHub either through the GitHub web UI or your local shell as follows:

    ```shell
    git push origin --delete my-feature-branch
    ```

* Check out the 1.0-develop branch:

    ```shell
    git checkout 1.0-develop -f
    ```

* Delete the local branch:

    ```shell
    git branch -D my-feature-branch
    ```

* Update your 1.0-develop with the latest upstream version:

    ```shell
    git pull --ff upstream 1.0-develop
    ```

[issue tracker]: https://github.com/marklogic/marklogic-data-hub/issues
[.editorconfig]: http://editorconfig.org/
