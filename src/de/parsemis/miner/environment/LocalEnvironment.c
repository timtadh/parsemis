/*
 * created Aug 17, 2006
 * 
 * @by Marc Woerlein (woerlein@informatik.uni-erlangen.de)
 *
 * Copyright 2006 Marc Woerlein
 * 
 * This file is part of parsemis.
 *
 * Licence: 
 *  LGPL: http://www.gnu.org/licenses/lgpl.html
 *   EPL: http://www.eclipse.org/org/documents/epl-v10.php
 *   See the LICENSE file in the project's top-level directory for details.
 */
#define _POSIX_SOURCE 1
#include <sys/times.h>
#include <unistd.h>
#include "de_parsemis_miner_environment_LocalEnvironment.h"


jlong Java_de_parsemis_miner_environment_LocalEnvironment_getCPUtime(JNIEnv *env, jobject this)
{
        struct tms tmsbuff;
        times(&tmsbuff);
        return (jlong)tmsbuff.tms_utime;
}

jlong Java_de_parsemis_miner_environment_LocalEnvironment_getClockTicks(JNIEnv *env, jobject this)
{
	return (jlong)sysconf(_SC_CLK_TCK);
}

