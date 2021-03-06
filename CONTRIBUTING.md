I want to design an asynchronous logger component, which can collect data from file, MySQL and etc asynchronously, and export data to somewhere like logstash, redis and etc. 
If you have any good ideas and suggestion, please open issues or pull request. Thanks for contributing!

**How to submit a PR?**

1. Fork repository

Please fork `Magpie` firstly.

2. Set up local repository

- You should run command as flow to clone code.

```
git clone https://github.com/<your_github_name>/Magpie.git
```
please replace the `your_github_name` with your github name.

- Add mstao/Magpie as upstream remote:

```
cd Magpie
git remote add upstream https://github.com/mstao/Magpie.git
```

- Check the local repository’s remotes

```
git remote -v
origin https://github.com/<your_github_name>/Magpie.git (fetch)
origin    https://github.com/<your_github_name>/Magpie.git (push)
upstream  https://github.com/mstao/Magpie.git (fetch)
upstream  https://github.com/mstao/Magpie.git (push)
```

- Create a new branch to start working

```
git checkout -b <your_branch_name>
```
Note: replace <your_branch_name> with an actual branch name at your choice, like feature-foo/bugfix-bar Now you can start coding.

- Push the changes to a remote repository

```
git commit -a -m "<you_commit_message>"
git push origin <your_branch_name>
```
3. Open a PR
![image](https://github.com/ZZULI-TECH/interview/blob/master/images/merge-test.png?raw=true)
