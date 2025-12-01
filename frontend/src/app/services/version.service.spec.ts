import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { VersionService } from './version.service';
import { Version, UserStory } from '../models/kanban.models';

describe('VersionService', () => {
    let service: VersionService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [VersionService]
        });
        service = TestBed.inject(VersionService);
        httpMock = TestBed.inject(HttpTestingController);
        localStorage.setItem('token', 'test-token');
    });

    afterEach(() => {
        httpMock.verify();
        localStorage.clear();
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should get versions by project', () => {
        const mockVersions: Version[] = [
            {
                id: 1,
                title: 'Version 1.0',
                description: 'First release',
                versionNumber: '1.0.0',
                status: 'PLANNED'
            },
            {
                id: 2,
                title: 'Version 1.1',
                description: 'Minor update',
                versionNumber: '1.1.0',
                status: 'IN_PROGRESS'
            }
        ];

        service.getByProject(1).subscribe(versions => {
            expect(versions).toEqual(mockVersions);
            expect(versions.length).toBe(2);
        });

        const req = httpMock.expectOne('/api/versions/project/1');
        expect(req.request.method).toBe('GET');
        expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
        req.flush(mockVersions);
    });

    it('should create a version', () => {
        const newVersion: Partial<Version> = {
            title: 'Version 2.0',
            description: 'Major update',
            versionNumber: '2.0.0'
        };

        const createdVersion: Version = {
            id: 3,
            title: 'Version 2.0',
            description: 'Major update',
            versionNumber: '2.0.0',
            status: 'PLANNED'
        };

        service.create({
            title: newVersion.title!,
            description: newVersion.description!,
            versionNumber: newVersion.versionNumber!,
            projectId: 1
        }).subscribe(version => {
            expect(version).toEqual(createdVersion);
        });

        const req = httpMock.expectOne('/api/versions');
        expect(req.request.method).toBe('POST');
        expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
        expect(req.request.body).toEqual({
            title: newVersion.title,
            description: newVersion.description,
            versionNumber: newVersion.versionNumber,
            projectId: 1
        });
        req.flush(createdVersion);

    });

    it('should update a version', () => {
        const updatedVersion: Partial<Version> = {
            title: 'Version 1.0 Updated',
            description: 'First release updated',
            versionNumber: '1.0.1'
        };

        const returnedVersion: Version = {
            id: 1,
            title: 'Version 1.0 Updated',
            description: 'First release updated',
            versionNumber: '1.0.1',
            status: 'PLANNED'
        };

        service.update(1, {
            title: updatedVersion.title!,
            description: updatedVersion.description!,
            versionNumber: updatedVersion.versionNumber!,
            projectId: 1
        }).subscribe(version => {
            expect(version).toEqual(returnedVersion);
        });

        const req = httpMock.expectOne('/api/versions/1');
        expect(req.request.method).toBe('PUT');
        expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
        expect(req.request.body).toEqual({
            title: updatedVersion.title,
            description: updatedVersion.description,
            versionNumber: updatedVersion.versionNumber,
            projectId: 1
        });
        req.flush(returnedVersion);

    });

    it('should delete a version', () => {
        service.delete(1).subscribe(response => {
            expect(response).toBeNull();
        });

        const req = httpMock.expectOne('/api/versions/1');
        expect(req.request.method).toBe('DELETE');
        expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
        req.flush(null);
    });

    it('should assign a user story to a version', () => {
        const mockUserStory: UserStory = {
            id: 1,
            title: 'User Story 1',
            description: 'Description 1',
            priority: 'MEDIUM',
            status: 'TODO'
        };

        service.assignUserStory(1, 1).subscribe(story => {
            expect(story).toEqual(mockUserStory);
        });

        const req = httpMock.expectOne('/api/versions/1/user-stories/1');
        expect(req.request.method).toBe('POST');
        expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
        req.flush(mockUserStory);
    });

    it('should remove a user story from a version', () => {
        const mockUserStory: UserStory = {
            id: 1,
            title: 'User Story 1',
            description: 'Description 1',
            priority: 'MEDIUM',
            status: 'TODO'
        };

        service.removeUserStory(1, 1).subscribe(story => {
            expect(story).toEqual(mockUserStory);
        });

        const req = httpMock.expectOne('/api/versions/1/user-stories/1');
        expect(req.request.method).toBe('DELETE');
        expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
        req.flush(mockUserStory);
    });


});