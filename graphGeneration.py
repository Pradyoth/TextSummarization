import pandas as pd
import networkx as nx
import matplotlib.pyplot as plt
import math

source = []
target = []
relation = []

filepath = 'D:/Study/Semester1/DataMining/Project/rel_output.txt'
with open(filepath) as fp:
   line = fp.readline()
   while line:
       #print(line)
       st = line.split('|')
       source.append(st[0])
       relation.append(st[1])
       target.append(st[2])
       line = fp.readline()

kg_df = pd.DataFrame({'source':source, 'target':target, 'edge':relation})
#print(kg_df)
G=nx.from_pandas_edgelist(kg_df, 'source', 'target', 
                          edge_attr='edge', create_using=nx.MultiDiGraph())

plt.figure(figsize=(15,15))

pos = nx.spring_layout(G,k=5/math.sqrt(G.order()))

arc_weight=G.get_edge_data(G,'edge')

nx.draw_networkx(G, pos, node_color='skyblue', node_size=850)
nx.draw_networkx_edges(G, pos)
nx.draw_networkx_edge_labels(G, pos, edge_labels=arc_weight)
plt.axis('off')
plt.savefig("graph.png")
plt.show()