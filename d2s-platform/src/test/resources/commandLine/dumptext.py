import os

filename=""

try:
   filename=os.environ["filepath"]
except KeyError:
   print "Please provide environment variable required (filepath|nReplicate)\n"
   sys.exit(1)


f = open(filename)

print f.read()

f.close()