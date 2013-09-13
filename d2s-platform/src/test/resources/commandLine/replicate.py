import os,sys

filename=""
nReplicate=""

try:
   filename=os.environ["filepath"]
   nReplicate = int(os.environ["nReplicate"])
except KeyError:
   print "Please provide environment variable required (filepath|nReplicate)\n"
   sys.exit(1)

for i in range(nReplicate):
    out=open(filename+str(i)+".out","w")
    f = open(filename)
    print filename
    out.write(f.read())
    out.close()


