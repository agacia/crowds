#!/usr/bin/python

"""A Python program

"""
import sys
import os
from optparse import OptionParser


# Define a main() function.
def main():
    # print sys.argv
    parser = OptionParser()
    parser.add_option('--inputFile', help=("inputFile."),
                      type="string", dest="inputFile")
    parser.add_option('--crowdsPath', help=("crowdsPath."),
                      type="string", dest="crowdsPath")
    parser.add_option('--crowdsjar', help=("crowdsjar."),
                      type="string", dest="crowdsjar")
    parser.add_option('--outputFolder', help=("outputFolder."),
                      type="string", dest="outputFolder")
    parser.add_option('--startStep', help=("Start time"),
                      type="int", dest='startStep')
    parser.add_option('--endStep', help=("End time"),
                      type="int", dest='endStep')
    parser.add_option('--numberOfIterations', help=("numberOfIterations"),
                      type="int", dest='numberOfIterations')
    parser.add_option('--speedHistoryLength', help=("speedHistoryLength"),
                      type="int", dest='speedHistoryLength')
    parser.add_option('--communityAlgorithmName', help=("communityAlgorithmName"),
                      type="string", dest='communityAlgorithmName')
    parser.add_option('--congestionAlgorithmName', help=("congestionAlgorithmName"),
                      type="string", dest='congestionAlgorithmName')
    parser.add_option('--speedType', help=("speedType"),
                      type="string", dest='speedType')
    parser.add_option('--goal', help=("goal"), type="string", dest='goal')
    parser.add_option('-c', '--cluster', help=("cluster"),
                      action="store_true", dest='cluster', default=False)

    (options, args) = parser.parse_args()
    print options

    # default options
    cluster = options.cluster or False
    if cluster:
        pass

    valid_dgs = check_dgs(options.inputFile)
    if not valid_dgs:
        print "DGS file not valid"
        return

    add_link_duration_dgs(options.inputFile)

    # run crodws
    app = os.path.join(options.crowdsPath, options.crowdsjar)
    args = " --inputFile %s --communityAlgorithmName %s --congestionAlgorithmName %s \
--outputDir %s --goal %s --startStep %s --endStep %s --numberOfIterations %s \
--speedHistoryLength %s --speedType %s " % (
            options.inputFile, options.communityAlgorithmName,
            options.congestionAlgorithmName,
            options.outputFolder, options.goal, options.startStep,
            options.endStep, options.numberOfIterations,
            options.speedHistoryLength, options.speedType)
    call_java(app, args)

    # analyse
    app = os.path.join(options.crowdsPath, "python", "analyse_crowds.py")
    inputfile = os.path.join(options.outputFolder, "crowds_communities.csv")
    args = " --inputFile %s --outputDir %s" % (
        inputfile, options.outputFolder)
    call_python(app, args)


def add_link_duration_dgs(dgspath):
    print "Adding link durations..."
    edges = {}
    new_dgs = open(dgspath+"_link_duration.dgs", "w")
    with open(dgspath) as f:
        for line in f:
            line = line.strip()
            data = line.split(" ")
            if len(data) > 0:
                if data[0] == "ae" or data[0] == "ce":
                    edgeid = data[1].strip()
                    edges[edgeid] = edges.get(edgeid, 0)
                    line = line + " linkDuration=%d" % (edges[edgeid])
                    if edges[edgeid] > 1:
                        print line
                if data[0] == "de":
                    edgeid = data[1].strip()
                    edges[edgeid] = edges.get(edgeid, 0) - 1
            new_dgs.write(line + "\n")
    print "Write link durations to file %s" % new_dgs


def check_dgs(dgspath):
    valid = True
    nodes = {}
    edges = {}
    with open(dgspath) as f:
        for line in f:
            data = line.split(" ")
            if len(data) > 0:
                if data[0] == "an":
                    nodeid = data[1].strip()
                    nodes[nodeid] = nodes.get(nodeid, 0) + 1
                if data[0] == "ae":
                    edgeid = data[1].strip()
                    edges[edgeid] = edges.get(edgeid, 0) + 1
                if data[0] == "de":
                    edgeid = data[1].strip()
                    edges[edgeid] = edges.get(edgeid, 0) - 1
        # check if a node or edge with the same id was created more than once
        doubled_node =  any( val > 1 for val in nodes.itervalues())
        num_nodes = len(nodes)
        doubled_edge =  any( val > 1 for val in edges.itervalues())
        num_edges = len(edges)
        if doubled_node:
            print "Doubled node!", doubled_node
            print [(k,v) for k,v in nodes.items() if v > 1]
            valid = False
        if doubled_edge:
            print "Doubled edge!", doubled_edge
            valid = False
        print "dgs file", dgspath, ", is valid: ", valid, ", num nodes: ", num_nodes, ", num edges", num_edges
    return valid


def call_java(app, args):
    call = "java -Xmx4096m -jar " +  app + " " + args
    print "running", call
    os.system(call)

def call_python(app, args):
    call = "python " + app + " " + args
    print "running", call
    # os.system(call)



if __name__ == '__main__':
    main()


