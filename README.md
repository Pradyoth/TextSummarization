# TextSummarization
Text Summarization using Knowledge Graphs

Build instruction (maybe move this to separate file later)
1. Clone git repo to your PC.
2. Include all the external jar files required.
3. Specifically remove jgrapht.jar, stanford-openie-{models,only-models,src,}.jar if included.
3. Check all filepaths before building.

How to parse the raw data:
1. Set the environment variable __PROJECT_HOME__ to root directory of the project.
e.g. __PROJECT_HOME__=_/home/user/workspace/TextSummarization_
2. Create folder named 'data' inside __PROJECT_HOME__.
3. Download and extract the CNN 'stories' from the link https://cs.nyu.edu/~kcho/DMQA/
to 'data' folder.
4. Run Parser.py, will take around 12 min to finish. It'll store the parsed files in
the 'data/parsed' directory. Each parsed file has two lines. First line is the
header (summary) and second is text. File name can be used as ID for each (summary,text) pair.
Directory structure should look like this

```bash
PROJECT_HOME
.
├── data
│   ├── parsed
│   │   ├──summary── 0001d1afc246a7964130f43ae940af6bc6c57f01.story
│   │	└──text── 0001d1afc246a7964130f43ae940af6bc6c57f01.story
│	├── output── 0001d1afc246a7964130f43ae940af6bc6c57f01.story
│	└── stories
│       └── 0001d1afc246a7964130f43ae940af6bc6c57f01.story
├── graphGeneration.py
├── PageRank.java
├── Parser.java
├── README.md
└── textSummarization.java
```
